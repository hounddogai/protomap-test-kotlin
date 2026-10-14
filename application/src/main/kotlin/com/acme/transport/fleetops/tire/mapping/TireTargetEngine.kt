package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tire target calculation. */
interface TireTargetFactor {
    fun describe(): String
    fun adjust(item: TireTarget, value: Double): Double
}

data class TireTargetRateOffsetAdjuster(var weight: Double) : TireTargetFactor {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: TireTarget, value: Double): Double = value + item.rate * weight
}

data class FlaggedTireTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTireTargetAdjuster(var percent: Double) : TireTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tire target steps in order and keeps a trace of each. */
data class TireTargetEngine(
    var steps: MutableList<TireTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TireTarget): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTireTargetEngine(flags: Registry): TireTargetEngine {
    return TireTargetEngine(
        steps = mutableListOf<TireTargetFactor>(
            TireTargetRateOffsetAdjuster(0.17),
            FlaggedTireTargetAdjuster(flags, "tire-target-scheduled", -1.2),
            PercentTireTargetAdjuster(-21.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 58582.7,
    )
}
