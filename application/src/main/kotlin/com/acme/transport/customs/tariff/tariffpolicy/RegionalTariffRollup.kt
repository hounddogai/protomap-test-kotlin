package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional tariff rollup is in its life cycle. */
enum class RegionalTariffRollupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a regional tariff rollup and its current status. */
data class RegionalTariffRollup(
    var code: String,
    var name: String,
    var category: String,
    var minimum: Double,
    var updatedAt: Int,
    var graceDays: Int,
    var externalRef: String,
    var loadKg: Double,
    var status: RegionalTariffRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTariffRollupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTariffRollupStatus): Boolean {
        when (status) {
            RegionalTariffRollupStatus.IDLE -> return next == RegionalTariffRollupStatus.WARMING || next == RegionalTariffRollupStatus.STOPPED
            RegionalTariffRollupStatus.WARMING -> {
                when (next) {
                    RegionalTariffRollupStatus.DRAINING, RegionalTariffRollupStatus.STOPPED -> return true
                }
                return false
            }
            RegionalTariffRollupStatus.READY -> return next == RegionalTariffRollupStatus.DRAINING || next == RegionalTariffRollupStatus.STOPPED
            RegionalTariffRollupStatus.DRAINING -> return next == RegionalTariffRollupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RegionalTariffRollupStatus) {
        if (!canMoveTo(next)) {
            error("regional tariff rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional tariff rollup"} $code"

    fun projectedTotal(): Double = round(loadKg * minimum * 100) / 100

    fun snapshot(): RegionalTariffRollup {
        return RegionalTariffRollup(
            code = code,
            name = name,
            category = category,
            minimum = minimum,
            updatedAt = updatedAt,
            graceDays = graceDays,
            externalRef = externalRef,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional tariff rollup in status idle. */
fun newRegionalTariffRollup(code: String, name: String): RegionalTariffRollup {
    return RegionalTariffRollup(
        code = code,
        name = name,
        category = "",
        minimum = 0.0,
        updatedAt = 0,
        graceDays = 0,
        externalRef = "",
        loadKg = 0.0,
        status = RegionalTariffRollupStatus.IDLE,
    )
}
