package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.kit.text.normalize

// The legacy service plan variance records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a legacy service plan variance is in its life cycle. */
enum class LegacyServicePlanVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A legacy service plan variance. Its code is unique within a tenant. */
data class LegacyServicePlanVariance(
    var code: String,
    var name: String,
    var timezone: String,
    var amount: Double,
    var vehicleId: String,
    var attributes: MutableMap<String, String>,
    var trailerId: String,
    var status: LegacyServicePlanVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyServicePlanVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyServicePlanVarianceStatus): Boolean {
        when (status) {
            LegacyServicePlanVarianceStatus.CREATED -> {
                when (next) {
                    LegacyServicePlanVarianceStatus.PUBLISHED, LegacyServicePlanVarianceStatus.RETIRED -> return true
                }
                return false
            }
            LegacyServicePlanVarianceStatus.VALIDATED -> return next == LegacyServicePlanVarianceStatus.RETIRED
            LegacyServicePlanVarianceStatus.PUBLISHED -> return next == LegacyServicePlanVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LegacyServicePlanVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy service plan variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = amount

    fun snapshot(): LegacyServicePlanVariance {
        return LegacyServicePlanVariance(
            code = code,
            name = name,
            timezone = timezone,
            amount = amount,
            vehicleId = vehicleId,
            attributes = attributes,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy service plan variance in status created. */
fun newLegacyServicePlanVariance(code: String, name: String): LegacyServicePlanVariance {
    return LegacyServicePlanVariance(
        code = code,
        name = name,
        timezone = "",
        amount = 0.0,
        vehicleId = "",
        attributes = mutableMapOf<String, String>(),
        trailerId = "",
        status = LegacyServicePlanVarianceStatus.CREATED,
    )
}
