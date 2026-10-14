package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rate card schedule calculation. */
interface RateCardScheduleModifier {
    fun describe(): String
    fun adjust(item: RateCardSchedule, value: Double): Double
}

data class TitleRateCardScheduleAdjuster(var factors: MutableMap<String, Double>) : RateCardScheduleModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RateCardSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRateCardScheduleAdjuster(var step: Double) : RateCardScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardSchedule, value: Double): Double = roundTo(value, step)
}

data class MinimumRateCardScheduleAdjuster(var minimum: Double) : RateCardScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRateCardScheduleAdjuster(var percent: Double) : RateCardScheduleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rate card schedule steps in order and keeps a trace of each. */
data class RateCardScheduleEngine(
    var steps: MutableList<RateCardScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RateCardSchedule): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRateCardScheduleEngine(): RateCardScheduleEngine {
    return RateCardScheduleEngine(
        steps = mutableListOf<RateCardScheduleModifier>(
            TitleRateCardScheduleAdjuster(
                mutableMapOf<String, Double>("north" to 1.28, "central" to 0.97, "retail" to 1.17),
            ),
            RoundingRateCardScheduleAdjuster(5.0),
            MinimumRateCardScheduleAdjuster(48.5),
            PercentRateCardScheduleAdjuster(-10.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 62027.4,
    )
}
