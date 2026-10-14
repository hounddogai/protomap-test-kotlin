package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive duty period segment is in its life cycle. */
enum class AdaptiveDutyPeriodSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a adaptive duty period segment and its current status. */
data class AdaptiveDutyPeriodSegment(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var title: String,
    var region: String,
    var ownerId: String,
    var segment: String,
    var margin: Double,
    var discount: Double,
    var durationMinutes: Int,
    var status: AdaptiveDutyPeriodSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDutyPeriodSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDutyPeriodSegmentStatus): Boolean {
        when (status) {
            AdaptiveDutyPeriodSegmentStatus.IDLE -> {
                when (next) {
                    AdaptiveDutyPeriodSegmentStatus.READY -> return true
                }
                return false
            }
            AdaptiveDutyPeriodSegmentStatus.WARMING -> {
                when (next) {
                    AdaptiveDutyPeriodSegmentStatus.READY, AdaptiveDutyPeriodSegmentStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveDutyPeriodSegmentStatus.READY -> return next == AdaptiveDutyPeriodSegmentStatus.STOPPED
            AdaptiveDutyPeriodSegmentStatus.DRAINING -> {
                when (next) {
                    AdaptiveDutyPeriodSegmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDutyPeriodSegmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive duty period segment $code cannot move from $status to $next")
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

    fun adjustedValue(): Double = round((margin + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveDutyPeriodSegment {
        return AdaptiveDutyPeriodSegment(
            code = code,
            name = name,
            tags = tags,
            title = title,
            region = region,
            ownerId = ownerId,
            segment = segment,
            margin = margin,
            discount = discount,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive duty period segment in status idle. */
fun newAdaptiveDutyPeriodSegment(code: String, name: String): AdaptiveDutyPeriodSegment {
    return AdaptiveDutyPeriodSegment(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        title = "",
        region = "",
        ownerId = "",
        segment = "",
        margin = 0.0,
        discount = 0.0,
        durationMinutes = 0,
        status = AdaptiveDutyPeriodSegmentStatus.IDLE,
    )
}
