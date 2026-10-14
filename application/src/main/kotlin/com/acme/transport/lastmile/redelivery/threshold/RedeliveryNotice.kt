package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery notice is in its life cycle. */
enum class RedeliveryNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A redelivery notice as the redelivery team keeps it. */
data class RedeliveryNotice(
    var code: String,
    var label: String,
    var minimum: Double,
    var startsAt: Int,
    var country: String,
    var status: RedeliveryNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryNoticeStatus): Boolean {
        when (status) {
            RedeliveryNoticeStatus.NEW -> return next == RedeliveryNoticeStatus.QUEUED
            RedeliveryNoticeStatus.QUEUED -> return next == RedeliveryNoticeStatus.DONE
            RedeliveryNoticeStatus.RUNNING -> {
                when (next) {
                    RedeliveryNoticeStatus.DONE, RedeliveryNoticeStatus.FAILED -> return true
                }
                return false
            }
            RedeliveryNoticeStatus.DONE -> return next == RedeliveryNoticeStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RedeliveryNoticeStatus) {
        if (!canMoveTo(next)) {
            error("redelivery notice $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"redelivery notice"} $code"

    fun adjustedVolume(): Double = round((minimum - startsAt.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryNotice {
        return RedeliveryNotice(
            code = code,
            label = label,
            minimum = minimum,
            startsAt = startsAt,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a redelivery notice in status new. */
fun newRedeliveryNotice(code: String, label: String): RedeliveryNotice {
    return RedeliveryNotice(
        code = code,
        label = label,
        minimum = 0.0,
        startsAt = 0,
        country = "",
        status = RedeliveryNoticeStatus.NEW,
    )
}
