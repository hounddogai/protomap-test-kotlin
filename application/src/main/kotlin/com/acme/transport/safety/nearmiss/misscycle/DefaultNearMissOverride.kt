package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default near miss override is in its life cycle. */
enum class DefaultNearMissOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default near miss override. Its code is unique within a tenant. */
data class DefaultNearMissOverride(
    var code: String,
    var label: String,
    var channel: String,
    var vehicleId: String,
    var trailerId: String,
    var weight: Double,
    var capacity: Int,
    var cost: Double,
    var maximum: Double,
    var status: DefaultNearMissOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultNearMissOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultNearMissOverrideStatus): Boolean {
        when (status) {
            DefaultNearMissOverrideStatus.PROPOSED -> return next == DefaultNearMissOverrideStatus.APPROVED
            DefaultNearMissOverrideStatus.APPROVED -> {
                when (next) {
                    DefaultNearMissOverrideStatus.REJECTED, DefaultNearMissOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultNearMissOverrideStatus.REJECTED -> return next == DefaultNearMissOverrideStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultNearMissOverrideStatus) {
        if (!canMoveTo(next)) {
            error("default near miss override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedValue(): Double = round((cost - maximum) * 100) / 100

    fun snapshot(): DefaultNearMissOverride {
        return DefaultNearMissOverride(
            code = code,
            label = label,
            channel = channel,
            vehicleId = vehicleId,
            trailerId = trailerId,
            weight = weight,
            capacity = capacity,
            cost = cost,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default near miss override in status proposed. */
fun newDefaultNearMissOverride(code: String, label: String): DefaultNearMissOverride {
    return DefaultNearMissOverride(
        code = code,
        label = label,
        channel = "",
        vehicleId = "",
        trailerId = "",
        weight = 0.0,
        capacity = 0,
        cost = 0.0,
        maximum = 0.0,
        status = DefaultNearMissOverrideStatus.PROPOSED,
    )
}
