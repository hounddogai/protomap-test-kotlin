package com.acme.transport.freightpay.audit.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict audit schedule is in its life cycle. */
enum class StrictAuditScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict audit schedule and its current status. */
data class StrictAuditSchedule(
    var code: String,
    var label: String,
    var attempts: Int,
    var retries: Int,
    var ceiling: Double,
    var active: Boolean,
    var fuelLiters: Double,
    var phone: String,
    var notes: String,
    var priority: Int,
    var status: StrictAuditScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAuditScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAuditScheduleStatus): Boolean {
        when (status) {
            StrictAuditScheduleStatus.PROPOSED -> {
                when (next) {
                    StrictAuditScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictAuditScheduleStatus.APPROVED -> return next == StrictAuditScheduleStatus.REJECTED
            StrictAuditScheduleStatus.REJECTED -> {
                when (next) {
                    StrictAuditScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictAuditScheduleStatus) {
        if (!canMoveTo(next)) {
            error("strict audit schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict audit schedule"} $code"

    fun effectiveVolume(): Double = round((retries.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): StrictAuditSchedule {
        return StrictAuditSchedule(
            code = code,
            label = label,
            attempts = attempts,
            retries = retries,
            ceiling = ceiling,
            active = active,
            fuelLiters = fuelLiters,
            phone = phone,
            notes = notes,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict audit schedule in status proposed. */
fun newStrictAuditSchedule(code: String, label: String): StrictAuditSchedule {
    return StrictAuditSchedule(
        code = code,
        label = label,
        attempts = 0,
        retries = 0,
        ceiling = 0.0,
        active = false,
        fuelLiters = 0.0,
        phone = "",
        notes = "",
        priority = 0,
        status = StrictAuditScheduleStatus.PROPOSED,
    )
}
