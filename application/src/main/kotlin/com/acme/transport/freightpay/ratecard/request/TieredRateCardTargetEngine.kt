package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered rate card target calculation. */
interface TieredRateCardTargetAdjuster {
    fun describe(): String
    fun adjust(item: TieredRateCardTarget, value: Double): Double
}

data class FlaggedTieredRateCardTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredRateCardTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredRateCardTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRateCardTargetCostOffsetAdjuster(var weight: Double) : TieredRateCardTargetAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: TieredRateCardTarget, value: Double): Double = value + item.cost * weight
}

data class MinimumTieredRateCardTargetAdjuster(var minimum: Double) : TieredRateCardTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredRateCardTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered rate card target steps in order and keeps a trace of each. */
data class TieredRateCardTargetEngine(
    var steps: MutableList<TieredRateCardTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredRateCardTarget): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredRateCardTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredRateCardTargetEngine(flags: Registry): TieredRateCardTargetEngine {
    return TieredRateCardTargetEngine(
        steps = mutableListOf<TieredRateCardTargetAdjuster>(
            FlaggedTieredRateCardTargetAdjuster(flags, "tiered-rate-card-target-default", -9.5),
            TieredRateCardTargetCostOffsetAdjuster(0.76),
            MinimumTieredRateCardTargetAdjuster(43.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 60480.8,
    )
}
