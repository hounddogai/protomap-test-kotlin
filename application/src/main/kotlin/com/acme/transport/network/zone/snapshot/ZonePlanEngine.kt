package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the zone plan calculation. */
interface ZonePlanModifier {
    fun describe(): String
    fun adjust(item: ZonePlan, value: Double): Double
}

data class CapZonePlanAdjuster(var cap: Double) : ZonePlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZonePlan, value: Double): Double = minOf(value, cap)
}

data class LocaleZonePlanAdjuster(var factors: MutableMap<String, Double>) : ZonePlanModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: ZonePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredZonePlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZonePlanModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZonePlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ZonePlanRatioOffsetAdjuster(var weight: Double) : ZonePlanModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: ZonePlan, value: Double): Double = value + item.ratio * weight
}

data class RoundingZonePlanAdjuster(var step: Double) : ZonePlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZonePlan, value: Double): Double = roundTo(value, step)
}

/** Runs the zone plan steps in order and keeps a trace of each. */
data class ZonePlanEngine(
    var steps: MutableList<ZonePlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ZonePlan): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ZonePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultZonePlanEngine(): ZonePlanEngine {
    return ZonePlanEngine(
        steps = mutableListOf<ZonePlanModifier>(
            CapZonePlanAdjuster(7223.0),
            LocaleZonePlanAdjuster(mutableMapOf<String, Double>("west" to 1.1, "central" to 1.37)),
            TieredZonePlanAdjuster(mutableListOf<Double>(669.0, 8610.0), mutableListOf<Double>(1.19, 1.08)),
            ZonePlanRatioOffsetAdjuster(0.19),
            RoundingZonePlanAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 7434.5,
    )
}
