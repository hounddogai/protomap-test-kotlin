package com.acme.transport.customs.tariff.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy tariff quota calculation. */
interface LegacyTariffQuotaStep {
    fun describe(): String
    fun adjust(item: LegacyTariffQuota, value: Double): Double
}

data class TieredLegacyTariffQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyTariffQuotaStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyTariffQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLegacyTariffQuotaAdjuster(var cap: Double) : LegacyTariffQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyTariffQuota, value: Double): Double = minOf(value, cap)
}

data class MinimumLegacyTariffQuotaAdjuster(var minimum: Double) : LegacyTariffQuotaStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyTariffQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLegacyTariffQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTariffQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTariffQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLegacyTariffQuotaAdjuster(var step: Double) : LegacyTariffQuotaStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTariffQuota, value: Double): Double = roundTo(value, step)
}

data class LegacyTariffQuotaThresholdOffsetAdjuster(var weight: Double) : LegacyTariffQuotaStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: LegacyTariffQuota, value: Double): Double = value + item.threshold * weight
}

/** Runs the legacy tariff quota steps in order and keeps a trace of each. */
data class LegacyTariffQuotaEngine(
    var steps: MutableList<LegacyTariffQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyTariffQuota): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTariffQuotaStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyTariffQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyTariffQuotaEngine(flags: Registry): LegacyTariffQuotaEngine {
    return LegacyTariffQuotaEngine(
        steps = mutableListOf<LegacyTariffQuotaStep>(
            TieredLegacyTariffQuotaAdjuster(
                mutableListOf<Double>(2074.0, 4924.0, 5140.0, 9446.0),
                mutableListOf<Double>(0.95, 0.91, 1.15, 0.86),
            ),
            CapLegacyTariffQuotaAdjuster(34475.0),
            MinimumLegacyTariffQuotaAdjuster(3.4),
            FlaggedLegacyTariffQuotaAdjuster(flags, "legacy-tariff-quota-pending", 9.9),
            RoundingLegacyTariffQuotaAdjuster(0.05),
            LegacyTariffQuotaThresholdOffsetAdjuster(1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 38974.1,
    )
}
