package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary service plan preference is in its life cycle. */
enum class PrimaryServicePlanPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary service plan preference as the service plan team keeps it. */
data class PrimaryServicePlanPreference(
    var code: String,
    var name: String,
    var graceDays: Int,
    var attempts: Int,
    var quantity: Int,
    var driverId: String,
    var depot: String,
    var active: Boolean,
    var priority: Int,
    var checksum: String,
    var vehicleId: String,
    var status: PrimaryServicePlanPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServicePlanPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServicePlanPreferenceStatus): Boolean {
        when (status) {
            PrimaryServicePlanPreferenceStatus.IDLE -> {
                when (next) {
                    PrimaryServicePlanPreferenceStatus.READY, PrimaryServicePlanPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryServicePlanPreferenceStatus.WARMING -> return next == PrimaryServicePlanPreferenceStatus.STOPPED
            PrimaryServicePlanPreferenceStatus.READY -> {
                when (next) {
                    PrimaryServicePlanPreferenceStatus.DRAINING, PrimaryServicePlanPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryServicePlanPreferenceStatus.DRAINING -> {
                when (next) {
                    PrimaryServicePlanPreferenceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryServicePlanPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("primary service plan preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary service plan preference"} $code"

    fun effectiveValue(): Double = round((attempts.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): PrimaryServicePlanPreference {
        return PrimaryServicePlanPreference(
            code = code,
            name = name,
            graceDays = graceDays,
            attempts = attempts,
            quantity = quantity,
            driverId = driverId,
            depot = depot,
            active = active,
            priority = priority,
            checksum = checksum,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a primary service plan preference in status idle. */
fun newPrimaryServicePlanPreference(code: String, name: String): PrimaryServicePlanPreference {
    return PrimaryServicePlanPreference(
        code = code,
        name = name,
        graceDays = 0,
        attempts = 0,
        quantity = 0,
        driverId = "",
        depot = "",
        active = false,
        priority = 0,
        checksum = "",
        vehicleId = "",
        status = PrimaryServicePlanPreferenceStatus.IDLE,
    )
}
