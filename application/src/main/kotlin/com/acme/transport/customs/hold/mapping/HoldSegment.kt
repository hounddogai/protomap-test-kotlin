package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold segment is in its life cycle. */
enum class HoldSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A hold segment. Its code is unique within a tenant. */
data class HoldSegment(
    var code: String,
    var title: String,
    var windowDays: Int,
    var notes: String,
    var rate: Double,
    var margin: Double,
    var weight: Double,
    var name: String,
    var status: HoldSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldSegmentStatus): Boolean {
        when (status) {
            HoldSegmentStatus.IDLE -> {
                when (next) {
                    HoldSegmentStatus.WARMING -> return true
                }
                return false
            }
            HoldSegmentStatus.WARMING -> return next == HoldSegmentStatus.STOPPED
            HoldSegmentStatus.READY -> {
                when (next) {
                    HoldSegmentStatus.DRAINING -> return true
                }
                return false
            }
            HoldSegmentStatus.DRAINING -> return next == HoldSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: HoldSegmentStatus) {
        if (!canMoveTo(next)) {
            error("hold segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round(rate * margin * 100) / 100

    fun snapshot(): HoldSegment {
        return HoldSegment(
            code = code,
            title = title,
            windowDays = windowDays,
            notes = notes,
            rate = rate,
            margin = margin,
            weight = weight,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hold segment in status idle. */
fun newHoldSegment(code: String, title: String): HoldSegment {
    return HoldSegment(
        code = code,
        title = title,
        windowDays = 0,
        notes = "",
        rate = 0.0,
        margin = 0.0,
        weight = 0.0,
        name = "",
        status = HoldSegmentStatus.IDLE,
    )
}
