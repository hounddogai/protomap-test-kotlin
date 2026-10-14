package com.acme.transport.terminal.appointment.appointmentsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy appointment segment records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a legacy appointment segment is in its life cycle. */
enum class LegacyAppointmentSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a legacy appointment segment and its current status. */
data class LegacyAppointmentSegment(
    var code: String,
    var name: String,
    var country: String,
    var active: Boolean,
    var startsAt: Int,
    var rate: Double,
    var externalRef: String,
    var label: String,
    var status: LegacyAppointmentSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAppointmentSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAppointmentSegmentStatus): Boolean {
        when (status) {
            LegacyAppointmentSegmentStatus.OPEN -> return next == LegacyAppointmentSegmentStatus.REOPENED
            LegacyAppointmentSegmentStatus.IN_REVIEW -> {
                when (next) {
                    LegacyAppointmentSegmentStatus.RESOLVED, LegacyAppointmentSegmentStatus.REOPENED -> return true
                }
                return false
            }
            LegacyAppointmentSegmentStatus.RESOLVED -> {
                when (next) {
                    LegacyAppointmentSegmentStatus.REOPENED, LegacyAppointmentSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyAppointmentSegmentStatus.REOPENED -> return next == LegacyAppointmentSegmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyAppointmentSegmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy appointment segment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy appointment segment"} $code"

    fun weightedLoad(): Double = round((startsAt.toDouble() + rate) * 100) / 100

    fun snapshot(): LegacyAppointmentSegment {
        return LegacyAppointmentSegment(
            code = code,
            name = name,
            country = country,
            active = active,
            startsAt = startsAt,
            rate = rate,
            externalRef = externalRef,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy appointment segment in status open. */
fun newLegacyAppointmentSegment(code: String, name: String): LegacyAppointmentSegment {
    return LegacyAppointmentSegment(
        code = code,
        name = name,
        country = "",
        active = false,
        startsAt = 0,
        rate = 0.0,
        externalRef = "",
        label = "",
        status = LegacyAppointmentSegmentStatus.OPEN,
    )
}
