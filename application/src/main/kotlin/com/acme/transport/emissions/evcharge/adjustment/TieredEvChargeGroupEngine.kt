package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered ev charge group calculation. */
interface TieredEvChargeGroupStep {
    fun describe(): String
    fun adjust(item: TieredEvChargeGroup, value: Double): Double
}

data class CapTieredEvChargeGroupAdjuster(var cap: Double) : TieredEvChargeGroupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredEvChargeGroup, value: Double): Double = minOf(value, cap)
}

data class PercentTieredEvChargeGroupAdjuster(var percent: Double) : TieredEvChargeGroupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredEvChargeGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredEvChargeGroupSequenceOffsetAdjuster(var weight: Double) : TieredEvChargeGroupStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: TieredEvChargeGroup, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class TieredTieredEvChargeGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredEvChargeGroupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredEvChargeGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTieredEvChargeGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredEvChargeGroupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredEvChargeGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered ev charge group steps in order and keeps a trace of each. */
data class TieredEvChargeGroupEngine(
    var steps: MutableList<TieredEvChargeGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredEvChargeGroup): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredEvChargeGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredEvChargeGroupEngine(flags: Registry): TieredEvChargeGroupEngine {
    return TieredEvChargeGroupEngine(
        steps = mutableListOf<TieredEvChargeGroupStep>(
            CapTieredEvChargeGroupAdjuster(48935.0),
            PercentTieredEvChargeGroupAdjuster(-19.4),
            TieredEvChargeGroupSequenceOffsetAdjuster(0.87),
            TieredTieredEvChargeGroupAdjuster(
                mutableListOf<Double>(2781.0, 7453.0, 7880.0),
                mutableListOf<Double>(0.83, 1.15, 0.83),
            ),
            FlaggedTieredEvChargeGroupAdjuster(flags, "tiered-ev-charge-group-batched", 1.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 15048.9,
    )
}
