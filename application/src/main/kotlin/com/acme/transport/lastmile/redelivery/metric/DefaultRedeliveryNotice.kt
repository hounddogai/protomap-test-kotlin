package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default redelivery notice is in its life cycle. */
enum class DefaultRedeliveryNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default redelivery notice and its current status. */
data class DefaultRedeliveryNotice(
    var code: String,
    var name: String,
    var capacity: Int,
    var retries: Int,
    var rate: Double,
    var ownerId: String,
    var attempts: Int,
    var discount: Double,
    var tags: MutableList<String>,
    var summary: String,
    var status: DefaultRedeliveryNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRedeliveryNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRedeliveryNoticeStatus): Boolean {
        when (status) {
            DefaultRedeliveryNoticeStatus.IDLE -> {
                when (next) {
                    DefaultRedeliveryNoticeStatus.STOPPED -> return true
                }
                return false
            }
            DefaultRedeliveryNoticeStatus.WARMING -> return next == DefaultRedeliveryNoticeStatus.READY
            DefaultRedeliveryNoticeStatus.READY -> return next == DefaultRedeliveryNoticeStatus.DRAINING
            DefaultRedeliveryNoticeStatus.DRAINING -> return next == DefaultRedeliveryNoticeStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultRedeliveryNoticeStatus) {
        if (!canMoveTo(next)) {
            error("default redelivery notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round((attempts.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): DefaultRedeliveryNotice {
        return DefaultRedeliveryNotice(
            code = code,
            name = name,
            capacity = capacity,
            retries = retries,
            rate = rate,
            ownerId = ownerId,
            attempts = attempts,
            discount = discount,
            tags = tags,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default redelivery notice in status idle. */
fun newDefaultRedeliveryNotice(code: String, name: String): DefaultRedeliveryNotice {
    return DefaultRedeliveryNotice(
        code = code,
        name = name,
        capacity = 0,
        retries = 0,
        rate = 0.0,
        ownerId = "",
        attempts = 0,
        discount = 0.0,
        tags = mutableListOf<String>(),
        summary = "",
        status = DefaultRedeliveryNoticeStatus.IDLE,
    )
}
