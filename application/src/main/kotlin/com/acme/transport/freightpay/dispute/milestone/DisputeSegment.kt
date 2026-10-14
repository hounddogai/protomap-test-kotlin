package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute segment is in its life cycle. */
enum class DisputeSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a dispute segment and its current status. */
data class DisputeSegment(
    var code: String,
    var title: String,
    var minimum: Double,
    var locale: String,
    var visible: Boolean,
    var ownerId: String,
    var threshold: Double,
    var budget: Double,
    var status: DisputeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeSegmentStatus): Boolean {
        when (status) {
            DisputeSegmentStatus.IDLE -> return next == DisputeSegmentStatus.WARMING
            DisputeSegmentStatus.WARMING -> return next == DisputeSegmentStatus.READY || next == DisputeSegmentStatus.DRAINING
            DisputeSegmentStatus.READY -> {
                when (next) {
                    DisputeSegmentStatus.DRAINING -> return true
                }
                return false
            }
            DisputeSegmentStatus.DRAINING -> return next == DisputeSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DisputeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("dispute segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedValue(): Double = round((minimum - threshold) * 100) / 100

    fun snapshot(): DisputeSegment {
        return DisputeSegment(
            code = code,
            title = title,
            minimum = minimum,
            locale = locale,
            visible = visible,
            ownerId = ownerId,
            threshold = threshold,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a dispute segment in status idle. */
fun newDisputeSegment(code: String, title: String): DisputeSegment {
    return DisputeSegment(
        code = code,
        title = title,
        minimum = 0.0,
        locale = "",
        visible = false,
        ownerId = "",
        threshold = 0.0,
        budget = 0.0,
        status = DisputeSegmentStatus.IDLE,
    )
}
