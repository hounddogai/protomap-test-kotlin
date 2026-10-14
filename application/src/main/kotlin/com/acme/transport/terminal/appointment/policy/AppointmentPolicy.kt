package com.acme.transport.terminal.appointment.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The appointment policy records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment policy is in its life cycle. */
enum class AppointmentPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A appointment policy. Its code is unique within a tenant. */
data class AppointmentPolicy(
    var code: String,
    var name: String,
    var quantity: Int,
    var capacity: Int,
    var unit: String,
    var pallets: Int,
    var weight: Double,
    var shiftCode: String,
    var status: AppointmentPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentPolicyStatus): Boolean {
        when (status) {
            AppointmentPolicyStatus.OPEN -> return next == AppointmentPolicyStatus.IN_REVIEW || next == AppointmentPolicyStatus.RESOLVED
            AppointmentPolicyStatus.IN_REVIEW -> return next == AppointmentPolicyStatus.REOPENED || next == AppointmentPolicyStatus.ARCHIVED
            AppointmentPolicyStatus.RESOLVED -> return next == AppointmentPolicyStatus.REOPENED
            AppointmentPolicyStatus.REOPENED -> {
                when (next) {
                    AppointmentPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentPolicyStatus) {
        if (!canMoveTo(next)) {
            error("appointment policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round(weight * capacity.toDouble() * 100) / 100

    fun snapshot(): AppointmentPolicy {
        return AppointmentPolicy(
            code = code,
            name = name,
            quantity = quantity,
            capacity = capacity,
            unit = unit,
            pallets = pallets,
            weight = weight,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a appointment policy in status open. */
fun newAppointmentPolicy(code: String, name: String): AppointmentPolicy {
    return AppointmentPolicy(
        code = code,
        name = name,
        quantity = 0,
        capacity = 0,
        unit = "",
        pallets = 0,
        weight = 0.0,
        shiftCode = "",
        status = AppointmentPolicyStatus.OPEN,
    )
}
