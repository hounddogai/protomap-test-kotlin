package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict inspection policy is in its life cycle. */
enum class StrictInspectionPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict inspection policy. Its code is unique within a tenant. */
data class StrictInspectionPolicy(
    var code: String,
    var label: String,
    var locale: String,
    var odometer: Int,
    var notes: String,
    var limit: Int,
    var status: StrictInspectionPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictInspectionPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictInspectionPolicyStatus): Boolean {
        when (status) {
            StrictInspectionPolicyStatus.PLANNED -> {
                when (next) {
                    StrictInspectionPolicyStatus.IN_PROGRESS -> return true
                }
                return false
            }
            StrictInspectionPolicyStatus.SCHEDULED -> {
                when (next) {
                    StrictInspectionPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            StrictInspectionPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    StrictInspectionPolicyStatus.COMPLETED -> return true
                }
                return false
            }
            StrictInspectionPolicyStatus.COMPLETED -> return next == StrictInspectionPolicyStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictInspectionPolicyStatus) {
        if (!canMoveTo(next)) {
            error("strict inspection policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round(limit.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): StrictInspectionPolicy {
        return StrictInspectionPolicy(
            code = code,
            label = label,
            locale = locale,
            odometer = odometer,
            notes = notes,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict inspection policy in status planned. */
fun newStrictInspectionPolicy(code: String, label: String): StrictInspectionPolicy {
    return StrictInspectionPolicy(
        code = code,
        label = label,
        locale = "",
        odometer = 0,
        notes = "",
        limit = 0,
        status = StrictInspectionPolicyStatus.PLANNED,
    )
}
