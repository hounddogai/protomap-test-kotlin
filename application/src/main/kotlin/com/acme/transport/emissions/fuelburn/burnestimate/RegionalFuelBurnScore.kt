package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.kit.text.normalize

/** Where a regional fuel burn score is in its life cycle. */
enum class RegionalFuelBurnScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional fuel burn score and its current status. */
data class RegionalFuelBurnScore(
    var code: String,
    var name: String,
    var stopCount: Int,
    var checksum: String,
    var unit: String,
    var status: RegionalFuelBurnScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelBurnScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelBurnScoreStatus): Boolean {
        when (status) {
            RegionalFuelBurnScoreStatus.REQUESTED -> return next == RegionalFuelBurnScoreStatus.GRANTED || next == RegionalFuelBurnScoreStatus.EXPIRED
            RegionalFuelBurnScoreStatus.GRANTED -> return next == RegionalFuelBurnScoreStatus.REVOKED || next == RegionalFuelBurnScoreStatus.EXPIRED
            RegionalFuelBurnScoreStatus.REVOKED -> {
                when (next) {
                    RegionalFuelBurnScoreStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalFuelBurnScoreStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel burn score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): RegionalFuelBurnScore {
        return RegionalFuelBurnScore(
            code = code,
            name = name,
            stopCount = stopCount,
            checksum = checksum,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional fuel burn score in status requested. */
fun newRegionalFuelBurnScore(code: String, name: String): RegionalFuelBurnScore {
    return RegionalFuelBurnScore(
        code = code,
        name = name,
        stopCount = 0,
        checksum = "",
        unit = "",
        status = RegionalFuelBurnScoreStatus.REQUESTED,
    )
}
