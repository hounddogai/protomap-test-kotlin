package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered availability policy calculation. */
interface TieredAvailabilityPolicyFactor {
    fun describe(): String
    fun adjust(item: TieredAvailabilityPolicy, value: Double): Double
}

data class TieredTieredAvailabilityPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredAvailabilityPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredAvailabilityPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTieredAvailabilityPolicyAdjuster(var cap: Double) : TieredAvailabilityPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredAvailabilityPolicy, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredAvailabilityPolicyAdjuster(var step: Double) : TieredAvailabilityPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredAvailabilityPolicy, value: Double): Double = roundTo(value, step)
}

data class TieredAvailabilityPolicyCeilingOffsetAdjuster(var weight: Double) : TieredAvailabilityPolicyFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: TieredAvailabilityPolicy, value: Double): Double = value + item.ceiling * weight
}

data class FlaggedTieredAvailabilityPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredAvailabilityPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredAvailabilityPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered availability policy steps in order and keeps a trace of each. */
data class TieredAvailabilityPolicyEngine(
    var steps: MutableList<TieredAvailabilityPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredAvailabilityPolicy): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredAvailabilityPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredAvailabilityPolicyEngine(flags: Registry): TieredAvailabilityPolicyEngine {
    return TieredAvailabilityPolicyEngine(
        steps = mutableListOf<TieredAvailabilityPolicyFactor>(
            TieredTieredAvailabilityPolicyAdjuster(
                mutableListOf<Double>(479.0, 2209.0, 2388.0, 4566.0),
                mutableListOf<Double>(0.87, 1.09, 1.17, 0.89),
            ),
            CapTieredAvailabilityPolicyAdjuster(3954.0),
            RoundingTieredAvailabilityPolicyAdjuster(0.25),
            TieredAvailabilityPolicyCeilingOffsetAdjuster(0.18),
            FlaggedTieredAvailabilityPolicyAdjuster(flags, "tiered-availability-policy-lenient", -5.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 12700.6,
    )
}
