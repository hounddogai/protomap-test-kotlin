package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered duty period digest calculation. */
interface TieredDutyPeriodDigestModifier {
    fun describe(): String
    fun adjust(item: TieredDutyPeriodDigest, value: Double): Double
}

data class FlaggedTieredDutyPeriodDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredDutyPeriodDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredDutyPeriodDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDutyPeriodDigestBudgetOffsetAdjuster(var weight: Double) : TieredDutyPeriodDigestModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: TieredDutyPeriodDigest, value: Double): Double = value + item.budget * weight
}

data class CapTieredDutyPeriodDigestAdjuster(var cap: Double) : TieredDutyPeriodDigestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredDutyPeriodDigest, value: Double): Double = minOf(value, cap)
}

data class MinimumTieredDutyPeriodDigestAdjuster(var minimum: Double) : TieredDutyPeriodDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDutyPeriodDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTieredDutyPeriodDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredDutyPeriodDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredDutyPeriodDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTieredDutyPeriodDigestAdjuster(var step: Double) : TieredDutyPeriodDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredDutyPeriodDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered duty period digest steps in order and keeps a trace of each. */
data class TieredDutyPeriodDigestEngine(
    var steps: MutableList<TieredDutyPeriodDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredDutyPeriodDigest): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredDutyPeriodDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredDutyPeriodDigestEngine(flags: Registry): TieredDutyPeriodDigestEngine {
    return TieredDutyPeriodDigestEngine(
        steps = mutableListOf<TieredDutyPeriodDigestModifier>(
            FlaggedTieredDutyPeriodDigestAdjuster(flags, "tiered-duty-period-digest-global", -5.4),
            TieredDutyPeriodDigestBudgetOffsetAdjuster(1.88),
            CapTieredDutyPeriodDigestAdjuster(23354.0),
            MinimumTieredDutyPeriodDigestAdjuster(14.9),
            TieredTieredDutyPeriodDigestAdjuster(
                mutableListOf<Double>(1205.0, 3422.0, 7020.0),
                mutableListOf<Double>(0.99, 1.07, 1.11),
            ),
            RoundingTieredDutyPeriodDigestAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 6980.5,
    )
}
