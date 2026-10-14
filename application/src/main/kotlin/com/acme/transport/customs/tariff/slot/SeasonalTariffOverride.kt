package com.acme.transport.customs.tariff.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal tariff override is in its life cycle. */
enum class SeasonalTariffOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One seasonal tariff override, with what the planners need to act on it. */
data class SeasonalTariffOverride(
    var code: String,
    var name: String,
    var maximum: Double,
    var budget: Double,
    var summary: String,
    var revision: Int,
    var score: Double,
    var visible: Boolean,
    var status: SeasonalTariffOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTariffOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTariffOverrideStatus): Boolean {
        when (status) {
            SeasonalTariffOverrideStatus.OPEN -> {
                when (next) {
                    SeasonalTariffOverrideStatus.IN_REVIEW, SeasonalTariffOverrideStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalTariffOverrideStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalTariffOverrideStatus.RESOLVED, SeasonalTariffOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalTariffOverrideStatus.RESOLVED -> return next == SeasonalTariffOverrideStatus.REOPENED || next == SeasonalTariffOverrideStatus.ARCHIVED
            SeasonalTariffOverrideStatus.REOPENED -> {
                when (next) {
                    SeasonalTariffOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTariffOverrideStatus) {
        if (!canMoveTo(next)) {
            error("seasonal tariff override $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"seasonal tariff override"} $code"

    fun netLoad(): Double = round(maximum * revision.toDouble() * 100) / 100

    fun snapshot(): SeasonalTariffOverride {
        return SeasonalTariffOverride(
            code = code,
            name = name,
            maximum = maximum,
            budget = budget,
            summary = summary,
            revision = revision,
            score = score,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal tariff override in status open. */
fun newSeasonalTariffOverride(code: String, name: String): SeasonalTariffOverride {
    return SeasonalTariffOverride(
        code = code,
        name = name,
        maximum = 0.0,
        budget = 0.0,
        summary = "",
        revision = 0,
        score = 0.0,
        visible = false,
        status = SeasonalTariffOverrideStatus.OPEN,
    )
}
