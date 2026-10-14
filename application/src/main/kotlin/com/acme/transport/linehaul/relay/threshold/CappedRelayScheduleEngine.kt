package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped relay schedule calculation. */
interface CappedRelayScheduleModifier {
    fun describe(): String
    fun adjust(item: CappedRelaySchedule, value: Double): Double
}

data class RegionCappedRelayScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedRelayScheduleModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: CappedRelaySchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedRelayScheduleAdjuster(var step: Double) : CappedRelayScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedRelaySchedule, value: Double): Double = roundTo(value, step)
}

data class TieredCappedRelayScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedRelayScheduleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedRelaySchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped relay schedule steps in order and keeps a trace of each. */
data class CappedRelayScheduleEngine(
    var steps: MutableList<CappedRelayScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedRelaySchedule): Double {
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

    fun evaluateAll(items: MutableList<CappedRelaySchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedRelayScheduleEngine(): CappedRelayScheduleEngine {
    return CappedRelayScheduleEngine(
        steps = mutableListOf<CappedRelayScheduleModifier>(
            RegionCappedRelayScheduleAdjuster(mutableMapOf<String, Double>("north" to 1.09, "central" to 1.18)),
            RoundingCappedRelayScheduleAdjuster(0.1),
            TieredCappedRelayScheduleAdjuster(
                mutableListOf<Double>(5268.0, 5648.0, 5769.0, 6082.0),
                mutableListOf<Double>(1.13, 1.14, 1.05, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 98822.6,
    )
}
