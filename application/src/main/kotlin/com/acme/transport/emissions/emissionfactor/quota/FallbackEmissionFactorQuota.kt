package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.kit.text.normalize

// The fallback emission factor quota records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a fallback emission factor quota is in its life cycle. */
enum class FallbackEmissionFactorQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fallback emission factor quota and its current status. */
data class FallbackEmissionFactorQuota(
    var code: String,
    var name: String,
    var vehicleId: String,
    var attributes: MutableMap<String, String>,
    var fuelLiters: Double,
    var locale: String,
    var status: FallbackEmissionFactorQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEmissionFactorQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEmissionFactorQuotaStatus): Boolean {
        when (status) {
            FallbackEmissionFactorQuotaStatus.IDLE -> return next == FallbackEmissionFactorQuotaStatus.WARMING || next == FallbackEmissionFactorQuotaStatus.DRAINING
            FallbackEmissionFactorQuotaStatus.WARMING -> {
                when (next) {
                    FallbackEmissionFactorQuotaStatus.STOPPED -> return true
                }
                return false
            }
            FallbackEmissionFactorQuotaStatus.READY -> return next == FallbackEmissionFactorQuotaStatus.DRAINING || next == FallbackEmissionFactorQuotaStatus.STOPPED
            FallbackEmissionFactorQuotaStatus.DRAINING -> return next == FallbackEmissionFactorQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackEmissionFactorQuotaStatus) {
        if (!canMoveTo(next)) {
            error("fallback emission factor quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): FallbackEmissionFactorQuota {
        return FallbackEmissionFactorQuota(
            code = code,
            name = name,
            vehicleId = vehicleId,
            attributes = attributes,
            fuelLiters = fuelLiters,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback emission factor quota in status idle. */
fun newFallbackEmissionFactorQuota(code: String, name: String): FallbackEmissionFactorQuota {
    return FallbackEmissionFactorQuota(
        code = code,
        name = name,
        vehicleId = "",
        attributes = mutableMapOf<String, String>(),
        fuelLiters = 0.0,
        locale = "",
        status = FallbackEmissionFactorQuotaStatus.IDLE,
    )
}
