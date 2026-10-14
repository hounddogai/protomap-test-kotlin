package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted appointment segment is in its life cycle. */
enum class WeightedAppointmentSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted appointment segment and its current status. */
data class WeightedAppointmentSegment(
    var code: String,
    var title: String,
    var priority: Int,
    var stopCount: Int,
    var contactEmail: String,
    var lane: String,
    var segment: String,
    var attributes: MutableMap<String, String>,
    var channel: String,
    var notes: String,
    var status: WeightedAppointmentSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAppointmentSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAppointmentSegmentStatus): Boolean {
        when (status) {
            WeightedAppointmentSegmentStatus.PLANNED -> return next == WeightedAppointmentSegmentStatus.COMPLETED || next == WeightedAppointmentSegmentStatus.CANCELLED
            WeightedAppointmentSegmentStatus.SCHEDULED -> {
                when (next) {
                    WeightedAppointmentSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedAppointmentSegmentStatus.IN_PROGRESS -> return next == WeightedAppointmentSegmentStatus.COMPLETED || next == WeightedAppointmentSegmentStatus.CANCELLED
            WeightedAppointmentSegmentStatus.COMPLETED -> return next == WeightedAppointmentSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedAppointmentSegmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted appointment segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted appointment segment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round(stopCount.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): WeightedAppointmentSegment {
        return WeightedAppointmentSegment(
            code = code,
            title = title,
            priority = priority,
            stopCount = stopCount,
            contactEmail = contactEmail,
            lane = lane,
            segment = segment,
            attributes = attributes,
            channel = channel,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted appointment segment in status planned. */
fun newWeightedAppointmentSegment(code: String, title: String): WeightedAppointmentSegment {
    return WeightedAppointmentSegment(
        code = code,
        title = title,
        priority = 0,
        stopCount = 0,
        contactEmail = "",
        lane = "",
        segment = "",
        attributes = mutableMapOf<String, String>(),
        channel = "",
        notes = "",
        status = WeightedAppointmentSegmentStatus.PLANNED,
    )
}
