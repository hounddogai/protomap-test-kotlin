package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling clearance policy is in its life cycle. */
enum class RollingClearancePolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling clearance policy as the clearance team keeps it. */
data class RollingClearancePolicy(
    var code: String,
    var title: String,
    var unit: String,
    var summary: String,
    var contactEmail: String,
    var vehicleId: String,
    var durationMinutes: Int,
    var label: String,
    var sequence: Int,
    var stopCount: Int,
    var version: Int,
    var status: RollingClearancePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingClearancePolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingClearancePolicyStatus): Boolean {
        when (status) {
            RollingClearancePolicyStatus.IDLE -> {
                when (next) {
                    RollingClearancePolicyStatus.STOPPED -> return true
                }
                return false
            }
            RollingClearancePolicyStatus.WARMING -> return next == RollingClearancePolicyStatus.READY || next == RollingClearancePolicyStatus.STOPPED
            RollingClearancePolicyStatus.READY -> return next == RollingClearancePolicyStatus.DRAINING
            RollingClearancePolicyStatus.DRAINING -> return next == RollingClearancePolicyStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingClearancePolicyStatus) {
        if (!canMoveTo(next)) {
            error("rolling clearance policy $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($title)"

    fun effectiveValue(): Double = round(stopCount.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): RollingClearancePolicy {
        return RollingClearancePolicy(
            code = code,
            title = title,
            unit = unit,
            summary = summary,
            contactEmail = contactEmail,
            vehicleId = vehicleId,
            durationMinutes = durationMinutes,
            label = label,
            sequence = sequence,
            stopCount = stopCount,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling clearance policy in status idle. */
fun newRollingClearancePolicy(code: String, title: String): RollingClearancePolicy {
    return RollingClearancePolicy(
        code = code,
        title = title,
        unit = "",
        summary = "",
        contactEmail = "",
        vehicleId = "",
        durationMinutes = 0,
        label = "",
        sequence = 0,
        stopCount = 0,
        version = 0,
        status = RollingClearancePolicyStatus.IDLE,
    )
}
