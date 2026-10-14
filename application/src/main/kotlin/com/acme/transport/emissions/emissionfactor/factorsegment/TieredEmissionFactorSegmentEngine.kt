package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered emission factor segment calculation. */
interface TieredEmissionFactorSegmentModifier {
    fun describe(): String
    fun adjust(item: TieredEmissionFactorSegment, value: Double): Double
}

data class FlaggedTieredEmissionFactorSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredEmissionFactorSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredEmissionFactorSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTieredEmissionFactorSegmentAdjuster(
    var minimum: Double,
) : TieredEmissionFactorSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredEmissionFactorSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotTieredEmissionFactorSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredEmissionFactorSegmentModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: TieredEmissionFactorSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered emission factor segment steps in order and keeps a trace of each. */
data class TieredEmissionFactorSegmentEngine(
    var steps: MutableList<TieredEmissionFactorSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredEmissionFactorSegment): Double {
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

    fun evaluateAll(items: MutableList<TieredEmissionFactorSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredEmissionFactorSegmentEngine(flags: Registry): TieredEmissionFactorSegmentEngine {
    return TieredEmissionFactorSegmentEngine(
        steps = mutableListOf<TieredEmissionFactorSegmentModifier>(
            FlaggedTieredEmissionFactorSegmentAdjuster(flags, "tiered-emission-factor-segment-capped", -2.2),
            MinimumTieredEmissionFactorSegmentAdjuster(38.0),
            DepotTieredEmissionFactorSegmentAdjuster(mutableMapOf<String, Double>("north" to 1.17)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 3029.2,
    )
}
