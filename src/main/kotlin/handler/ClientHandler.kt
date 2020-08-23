package handler

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpMessage
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpMethod
import data.TestData
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.charset.Charset

/*
    Copyright 2020 eu-pro
    Licensed under the Apache License, Version 2.0
 */

class ClientHandler(private val outgoingChannelSupplier: (Channel) -> Channel) : Handler() {
    private lateinit var outgoingChannel: Channel

    override fun channelActive(ctx: ChannelHandlerContext) {
        outgoingChannel = outgoingChannelSupplier(ctx.channel())
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        closeChannels(ctx.channel(), outgoingChannel)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error(cause)
        closeChannels(ctx.channel(), outgoingChannel)
    }

    private fun host(): String {
        val socketAddress: SocketAddress = outgoingChannel.remoteAddress()

        if (socketAddress is InetSocketAddress) {
            return "${socketAddress.getHostName()}:${socketAddress.getPort()}"
        } else {
            return socketAddress.toString()
        }
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: FullHttpMessage
    ) {
        var endpointUri = ""
        var method: HttpMethod = HttpMethod.POST
        logger.info("Request:")
        if (msg is FullHttpRequest && outgoingChannel.isActive) {
            endpointUri = msg.uri()
            method = msg.method()
            val hostPort = host()
            logger.info("METHOD HOST/ENDPOINT: ${msg.method()}:${hostPort}${msg.uri()}")
            msg.apply {
                uri = "http://$hostPort${msg.uri()}"
                headers().set(HttpHeaderNames.HOST, hostPort)
            }
                .also {
                    outgoingChannel.writeAndFlush(it.retain())
                }
        }

        try {
            val headersMap: Map<String, String> =
                msg.retain().headers().associateTo(mutableMapOf(), { it.key.toString() to it.value.toString() })
            val json: String =
                msg.content().getCharSequence(0, msg.content().capacity(), Charset.defaultCharset()).toString()

            TestData.instance.setRequestData(method, endpointUri, headersMap, json)
        } catch (e: Throwable) {
            logger.error(
                "Content length should be specified for PUT, POST requests. " +
                        "Otherwise it is impossible to get json string."
            , e)
        }
    }
}