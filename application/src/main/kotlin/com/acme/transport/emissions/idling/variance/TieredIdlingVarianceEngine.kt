package com.acme.transport.emissions.idling.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered idling variance calculation. */
interface TieredIdlingVarianceStep {
    fun describe(): String
    fun adjust(item: TieredIdlingVariance, value: Double): Double
}

data class CapTieredIdlingVarianceAdjuster(var cap: Double) : TieredIdlingVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredIdlingVariance, value: Double): Double = minOf(value, cap)
}

data class FlaggedTieredIdlingVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredIdlingVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredIdlingVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTieredIdlingVarianceAdjuster(var percent: Double) : TieredIdlingVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredIdlingVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameTieredIdlingVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredIdlingVarianceStep {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredIdlingVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredIdlingVarianceFloorValueOffsetAdjuster(var weight: Double) : TieredIdlingVarianceStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: TieredIdlingVariance, value: Double): Double = value + item.floorValue * weight
}

/** Runs the tiered idling variance steps in order and keeps a trace of each. */
data class TieredIdlingVarianceEngine(
    var steps: MutableList<TieredIdlingVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredIdlingVariance): Double {
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

    fun evaluateAll(items: MutableList<TieredIdlingVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredIdlingVarianceEngine(flags: Registry): TieredIdlingVarianceEngine {
    return TieredIdlingVarianceEngine(
        steps = mutableListOf<TieredIdlingVarianceStep>(
            CapTieredIdlingVarianceAdjuster(49704.0),
            FlaggedTieredIdlingVarianceAdjuster(flags, "tiered-idling-variance-weighted", 7.5),
            PercentTieredIdlingVarianceAdjuster(2.7),
            NameTieredIdlingVarianceAdjuster(mutableMapOf<String, Double>("central" to 1.39)),
            TieredIdlingVarianceFloorValueOffsetAdjuster(1.68),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 92730.5,
    )
}
