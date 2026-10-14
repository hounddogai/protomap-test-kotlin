package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient appointment mapping records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a lenient appointment mapping is in its life cycle. */
enum class LenientAppointmentMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient appointment mapping as the appointment team keeps it. */
data class LenientAppointmentMapping(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var depot: String,
    var checksum: String,
    var retries: Int,
    var windowDays: Int,
    var reviewer: String,
    var createdAt: Int,
    var externalRef: String,
    var active: Boolean,
    var status: LenientAppointmentMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAppointmentMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAppointmentMappingStatus): Boolean {
        when (status) {
            LenientAppointmentMappingStatus.PLANNED -> {
                when (next) {
                    LenientAppointmentMappingStatus.SCHEDULED, LenientAppointmentMappingStatus.COMPLETED -> return true
                }
                return false
            }
            LenientAppointmentMappingStatus.SCHEDULED -> return next == LenientAppointmentMappingStatus.IN_PROGRESS || next == LenientAppointmentMappingStatus.COMPLETED
            LenientAppointmentMappingStatus.IN_PROGRESS -> {
                when (next) {
                    LenientAppointmentMappingStatus.COMPLETED, LenientAppointmentMappingStatus.CANCELLED -> return true
                }
                return false
            }
            LenientAppointmentMappingStatus.COMPLETED -> return next == LenientAppointmentMappingStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientAppointmentMappingStatus) {
        if (!canMoveTo(next)) {
            error("lenient appointment mapping $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round((etaMinutes.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): LenientAppointmentMapping {
        return LenientAppointmentMapping(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            depot = depot,
            checksum = checksum,
            retries = retries,
            windowDays = windowDays,
            reviewer = reviewer,
            createdAt = createdAt,
            externalRef = externalRef,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient appointment mapping in status planned. */
fun newLenientAppointmentMapping(code: String, title: String): LenientAppointmentMapping {
    return LenientAppointmentMapping(
        code = code,
        title = title,
        etaMinutes = 0,
        depot = "",
        checksum = "",
        retries = 0,
        windowDays = 0,
        reviewer = "",
        createdAt = 0,
        externalRef = "",
        active = false,
        status = LenientAppointmentMappingStatus.PLANNED,
    )
}
