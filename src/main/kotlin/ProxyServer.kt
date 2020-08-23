import handler.ClientHandler
import handler.Handler
import handler.ServerHandler
import handler.closeChannels
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import java.net.InetSocketAddress
import java.net.SocketAddress

/*
    Copyright 2020 eu-pro
    Licensed under the Apache License, Version 2.0
 */

object ProxyServer {
    fun launch(remote: InetSocketAddress, local: InetSocketAddress) {
        val childGroup = NioEventLoopGroup(5)
        val parentGroup = NioEventLoopGroup(5)
        val groups = arrayOf(childGroup, parentGroup)

        try {
            val b = ServerBootstrap()
            b.group(parentGroup, childGroup)
            b.channel(NioServerSocketChannel::class.java)
            b.childHandler(object : ChannelInitializer<Channel>() {
                override fun initChannel(ch: Channel) {
                    with(ch.pipeline()) {
                        addLast(HttpServerCodec())
                        addLast(HttpObjectAggregator(8192, true))
                        addLast(ClientHandler(outgoingChannel(remote)))
                    }
                }
            })
            b.childOption(ChannelOption.AUTO_READ, false)
            val f: ChannelFuture = b.bind(local).sync()
            f.channel().closeFuture().sync()
        } finally {
            groups.forEach { it.shutdownGracefully().syncUninterruptibly() }
        }
    }

    fun outgoingChannel(remote: SocketAddress): (Channel) -> Channel = { outgoingChannel ->
        val b = Bootstrap()
        b.channel(outgoingChannel.javaClass)
        b.group(outgoingChannel.eventLoop())
        b.handler(object : ChannelInitializer<Channel>() {
            override fun initChannel(ch: Channel) {
                ch.pipeline().apply {
                    addLast(HttpClientCodec())
                    addLast(HttpObjectAggregator(8192, true))
                    addLast(ServerHandler(outgoingChannel))
                }
            }
        })
        b.option(ChannelOption.AUTO_READ, false)
        b.connect(remote).apply {
            addListener { f ->
                if (f.isSuccess) {
                    outgoingChannel.read()
                } else {
                    Handler.logger.error(f.cause())
                    closeChannels(outgoingChannel)
                }
            }
        }.channel()
    }
}

fun main() {
    //server should be launched on this ip and port
    val testServerUrl = InetSocketAddress("127.0.0.1", 8087)
    //client requests should be sent on this ip and port
    val proxyServerUrl = InetSocketAddress("127.0.0.1", 8088)
    ProxyServer.launch(testServerUrl, proxyServerUrl)
}