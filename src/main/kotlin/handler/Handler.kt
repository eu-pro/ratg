package handler

import io.netty.channel.Channel
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpMessage
import org.apache.logging.log4j.kotlin.Logging

/*
    Copyright 2020 eu-pro
    Licensed under the Apache License, Version 2.0
 */

abstract class Handler : SimpleChannelInboundHandler<FullHttpMessage>(), Logging {
    companion object : Logging
}

internal fun closeChannels(vararg channels: Channel) {
    channels.forEach { it.close() }
}
