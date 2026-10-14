package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the zone cycle calculation. */
interface ZoneCycleModifier {
    fun describe(): String
    fun adjust(item: ZoneCycle, value: Double): Double
}

data class PercentZoneCycleAdjuster(var percent: Double) : ZoneCycleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedZoneCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredZoneCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneCycleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingZoneCycleAdjuster(var step: Double) : ZoneCycleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneCycle, value: Double): Double = roundTo(value, step)
}

data class ZoneCycleLoadKgOffsetAdjuster(var weight: Double) : ZoneCycleModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: ZoneCycle, value: Double): Double = value + item.loadKg * weight
}

/** Runs the zone cycle steps in order and keeps a trace of each. */
data class ZoneCycleEngine(
    var steps: MutableList<ZoneCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ZoneCycle): Double {
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

    fun evaluateAll(items: MutableList<ZoneCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultZoneCycleEngine(flags: Registry): ZoneCycleEngine {
    return ZoneCycleEngine(
        steps = mutableListOf<ZoneCycleModifier>(
            PercentZoneCycleAdjuster(-24.8),
            FlaggedZoneCycleAdjuster(flags, "zone-cycle-manual", -4.7),
            TieredZoneCycleAdjuster(
                mutableListOf<Double>(4800.0, 5639.0, 6760.0, 9486.0),
                mutableListOf<Double>(1.01, 1.15, 0.84, 0.88),
            ),
            RoundingZoneCycleAdjuster(0.5),
            ZoneCycleLoadKgOffsetAdjuster(0.79),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 50929.2,
    )
}
