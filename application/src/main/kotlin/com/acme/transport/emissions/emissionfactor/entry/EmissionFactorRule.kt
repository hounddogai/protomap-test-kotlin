package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.text.normalize

/** Where a emission factor rule is in its life cycle. */
enum class EmissionFactorRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A emission factor rule as the emission factor team keeps it. */
data class EmissionFactorRule(
    var code: String,
    var name: String,
    var channel: String,
    var checksum: String,
    var pinned: Boolean,
    var fuelLiters: Double,
    var status: EmissionFactorRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorRuleStatus): Boolean {
        when (status) {
            EmissionFactorRuleStatus.PLANNED -> return next == EmissionFactorRuleStatus.IN_PROGRESS || next == EmissionFactorRuleStatus.COMPLETED
            EmissionFactorRuleStatus.SCHEDULED -> return next == EmissionFactorRuleStatus.IN_PROGRESS || next == EmissionFactorRuleStatus.CANCELLED
            EmissionFactorRuleStatus.IN_PROGRESS -> return next == EmissionFactorRuleStatus.COMPLETED || next == EmissionFactorRuleStatus.CANCELLED
            EmissionFactorRuleStatus.COMPLETED -> {
                when (next) {
                    EmissionFactorRuleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorRuleStatus) {
        if (!canMoveTo(next)) {
            error("emission factor rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"emission factor rule"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): EmissionFactorRule {
        return EmissionFactorRule(
            code = code,
            name = name,
            channel = channel,
            checksum = checksum,
            pinned = pinned,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a emission factor rule in status planned. */
fun newEmissionFactorRule(code: String, name: String): EmissionFactorRule {
    return EmissionFactorRule(
        code = code,
        name = name,
        channel = "",
        checksum = "",
        pinned = false,
        fuelLiters = 0.0,
        status = EmissionFactorRuleStatus.PLANNED,
    )
}
