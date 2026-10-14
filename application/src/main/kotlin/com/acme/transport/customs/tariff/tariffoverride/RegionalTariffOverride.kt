package com.acme.transport.customs.tariff.tariffoverride

import com.acme.transport.kit.text.normalize

// The regional tariff override records of customs's tariff work, and the rules, stores, and calculations
// around them.

/** Where a regional tariff override is in its life cycle. */
enum class RegionalTariffOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional tariff override. Its code is unique within a tenant. */
data class RegionalTariffOverride(
    var code: String,
    var name: String,
    var maximum: Double,
    var description: String,
    var trailerId: String,
    var source: String,
    var status: RegionalTariffOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTariffOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTariffOverrideStatus): Boolean {
        when (status) {
            RegionalTariffOverrideStatus.REQUESTED -> {
                when (next) {
                    RegionalTariffOverrideStatus.GRANTED -> return true
                }
                return false
            }
            RegionalTariffOverrideStatus.GRANTED -> return next == RegionalTariffOverrideStatus.REVOKED || next == RegionalTariffOverrideStatus.EXPIRED
            RegionalTariffOverrideStatus.REVOKED -> return next == RegionalTariffOverrideStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalTariffOverrideStatus) {
        if (!canMoveTo(next)) {
            error("regional tariff override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional tariff override"} $code"

    fun measuredValue(): Double = maximum

    fun snapshot(): RegionalTariffOverride {
        return RegionalTariffOverride(
            code = code,
            name = name,
            maximum = maximum,
            description = description,
            trailerId = trailerId,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional tariff override in status requested. */
fun newRegionalTariffOverride(code: String, name: String): RegionalTariffOverride {
    return RegionalTariffOverride(
        code = code,
        name = name,
        maximum = 0.0,
        description = "",
        trailerId = "",
        source = "",
        status = RegionalTariffOverrideStatus.REQUESTED,
    )
}
