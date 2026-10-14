package com.acme.transport.terminal.appointment.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback appointment entry records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a fallback appointment entry is in its life cycle. */
enum class FallbackAppointmentEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a fallback appointment entry and its current status. */
data class FallbackAppointmentEntry(
    var code: String,
    var title: String,
    var threshold: Double,
    var checksum: String,
    var description: String,
    var priority: Int,
    var loadKg: Double,
    var status: FallbackAppointmentEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAppointmentEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAppointmentEntryStatus): Boolean {
        when (status) {
            FallbackAppointmentEntryStatus.DRAFT -> {
                when (next) {
                    FallbackAppointmentEntryStatus.ACTIVE -> return true
                }
                return false
            }
            FallbackAppointmentEntryStatus.PENDING -> return next == FallbackAppointmentEntryStatus.SUSPENDED || next == FallbackAppointmentEntryStatus.CLOSED
            FallbackAppointmentEntryStatus.ACTIVE -> return next == FallbackAppointmentEntryStatus.CLOSED
            FallbackAppointmentEntryStatus.SUSPENDED -> {
                when (next) {
                    FallbackAppointmentEntryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackAppointmentEntryStatus) {
        if (!canMoveTo(next)) {
            error("fallback appointment entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback appointment entry"} $code"

    fun projectedValue(): Double = round((priority.toDouble() - loadKg) * 100) / 100

    fun snapshot(): FallbackAppointmentEntry {
        return FallbackAppointmentEntry(
            code = code,
            title = title,
            threshold = threshold,
            checksum = checksum,
            description = description,
            priority = priority,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback appointment entry in status draft. */
fun newFallbackAppointmentEntry(code: String, title: String): FallbackAppointmentEntry {
    return FallbackAppointmentEntry(
        code = code,
        title = title,
        threshold = 0.0,
        checksum = "",
        description = "",
        priority = 0,
        loadKg = 0.0,
        status = FallbackAppointmentEntryStatus.DRAFT,
    )
}
