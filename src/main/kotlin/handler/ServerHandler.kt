package handler

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpMessage
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import data.TestData
import java.nio.charset.Charset

/*
    Copyright 2020 eu-pro
    Licensed under the Apache License, Version 2.0
 */

class ServerHandler(private val outgoingChannel: Channel) : Handler() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.read()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        closeChannels(ctx.channel(), outgoingChannel)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error(cause)
        closeChannels(ctx.channel(), outgoingChannel)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpMessage) {
        var status: HttpResponseStatus
        logger.info("Response:")
        (msg as FullHttpResponse)
            .also {
                logger.info("STATUS:  ${msg.status()}")
                status = msg.status()
                outgoingChannel.writeAndFlush(it.retain())
                    .addListener {
                        closeChannels(ctx.channel(), outgoingChannel)
                    }
            }

        val headersMap: Map<String, String> =
            msg.retain().headers()
                .associateTo(mutableMapOf<String, String>(), { it.key.toString() to it.value.toString() })
        val json: String =
            msg.content().getCharSequence(0, msg.content().capacity(), Charset.defaultCharset()).toString()

        TestData.instance.setResponseData(headersMap, json, status)
    }
}