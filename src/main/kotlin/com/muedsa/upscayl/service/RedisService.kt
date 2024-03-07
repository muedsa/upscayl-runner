package com.muedsa.upscayl.service

import com.muedsa.upscayl.configuration.RedisConfig
import com.muedsa.upscayl.di.KoinShutdownDispatcher
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import org.slf4j.LoggerFactory

class RedisService(
    redisConfig: RedisConfig
) {
    private val logger = LoggerFactory.getLogger(RedisService::class.java)

    private val redisUri: RedisURI = RedisURI.Builder.redis(redisConfig.host, redisConfig.port)
        .withPassword(redisConfig.password.toCharArray())
        .build()
    private val redisClient: RedisClient = RedisClient.create(redisUri)
    private val connection: StatefulRedisConnection<String, String> = redisClient.connect()
    private val commands = connection.sync()
    private val pubSubConnection: StatefulRedisPubSubConnection<String, String> = redisClient.connectPubSub()
    private val subDispatcher = RedisSubDispatcher()

    init {
        pubSubConnection.addListener(subDispatcher)
        KoinShutdownDispatcher.register {
            connection.close()
            pubSubConnection.close()
            redisClient.shutdown()
        }
    }

    fun addListener(listener: RedisChannelListener) {
        subDispatcher.addListener(listener)
    }

    fun setNx(key: String, value: String, expire: Long): Boolean {
        return commands.setnx(key, value) && commands.expire(key, expire)
    }

    fun del(key: String): Boolean {
        return commands.del(key) > 0
    }

    inner class RedisSubDispatcher : RedisPubSubAdapter<String, String>() {

        private val channelListeners = mutableMapOf<String, MutableList<RedisChannelListener>>()

        override fun message(channel: String?, message: String?) {
            channelListeners[channel]?.forEach { listener ->
                try {
                    listener.message(message!!)
                } catch (t: Throwable) {
                    logger.error("Error in listener", t)
                }
            }
        }

        fun addListener(listener: RedisChannelListener) {
            if (channelListeners[listener.channel] == null) {
                channelListeners[listener.channel] = mutableListOf()
            }
            channelListeners[listener.channel]!!.add(listener)
            if (channelListeners[listener.channel]!!.size == 1) {
                pubSubConnection.sync().subscribe(listener.channel)
            }
        }
    }
}

abstract class RedisChannelListener(
    val channel: String
) {
    abstract fun message(message: String)
}