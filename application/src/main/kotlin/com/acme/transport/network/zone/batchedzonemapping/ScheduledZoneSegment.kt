package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.text.normalize

/** Where a scheduled zone segment is in its life cycle. */
enum class ScheduledZoneSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scheduled zone segment and its current status. */
data class ScheduledZoneSegment(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var channel: String,
    var lane: String,
    var quantity: Int,
    var status: ScheduledZoneSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledZoneSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledZoneSegmentStatus): Boolean {
        when (status) {
            ScheduledZoneSegmentStatus.CREATED -> return next == ScheduledZoneSegmentStatus.RETIRED
            ScheduledZoneSegmentStatus.VALIDATED -> {
                when (next) {
                    ScheduledZoneSegmentStatus.PUBLISHED, ScheduledZoneSegmentStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledZoneSegmentStatus.PUBLISHED -> return next == ScheduledZoneSegmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledZoneSegmentStatus) {
        if (!canMoveTo(next)) {
            error("scheduled zone segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): ScheduledZoneSegment {
        return ScheduledZoneSegment(
            code = code,
            title = title,
            pinned = pinned,
            channel = channel,
            lane = lane,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a scheduled zone segment in status created. */
fun newScheduledZoneSegment(code: String, title: String): ScheduledZoneSegment {
    return ScheduledZoneSegment(
        code = code,
        title = title,
        pinned = false,
        channel = "",
        lane = "",
        quantity = 0,
        status = ScheduledZoneSegmentStatus.CREATED,
    )
}
