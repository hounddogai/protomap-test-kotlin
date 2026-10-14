package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled transit time digest is in its life cycle. */
enum class ScheduledTransitTimeDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a scheduled transit time digest and its current status. */
data class ScheduledTransitTimeDigest(
    var code: String,
    var title: String,
    var stopCount: Int,
    var budget: Double,
    var visible: Boolean,
    var checksum: String,
    var category: String,
    var discount: Double,
    var status: ScheduledTransitTimeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTransitTimeDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTransitTimeDigestStatus): Boolean {
        when (status) {
            ScheduledTransitTimeDigestStatus.IDLE -> return next == ScheduledTransitTimeDigestStatus.STOPPED
            ScheduledTransitTimeDigestStatus.WARMING -> {
                when (next) {
                    ScheduledTransitTimeDigestStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledTransitTimeDigestStatus.READY -> {
                when (next) {
                    ScheduledTransitTimeDigestStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledTransitTimeDigestStatus.DRAINING -> return next == ScheduledTransitTimeDigestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledTransitTimeDigestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled transit time digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled transit time digest"} $code"

    fun weightedLoad(): Double = round((stopCount.toDouble() - discount) * 100) / 100

    fun snapshot(): ScheduledTransitTimeDigest {
        return ScheduledTransitTimeDigest(
            code = code,
            title = title,
            stopCount = stopCount,
            budget = budget,
            visible = visible,
            checksum = checksum,
            category = category,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled transit time digest in status idle. */
fun newScheduledTransitTimeDigest(code: String, title: String): ScheduledTransitTimeDigest {
    return ScheduledTransitTimeDigest(
        code = code,
        title = title,
        stopCount = 0,
        budget = 0.0,
        visible = false,
        checksum = "",
        category = "",
        discount = 0.0,
        status = ScheduledTransitTimeDigestStatus.IDLE,
    )
}
