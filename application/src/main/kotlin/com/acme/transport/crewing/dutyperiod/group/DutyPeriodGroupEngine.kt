package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the duty period group calculation. */
interface DutyPeriodGroupModifier {
    fun describe(): String
    fun adjust(item: DutyPeriodGroup, value: Double): Double
}

data class TieredDutyPeriodGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDutyPeriodGroupAdjuster(var minimum: Double) : DutyPeriodGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDutyPeriodGroupAdjuster(var step: Double) : DutyPeriodGroupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodGroup, value: Double): Double = roundTo(value, step)
}

data class TitleDutyPeriodGroupAdjuster(var factors: MutableMap<String, Double>) : DutyPeriodGroupModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: DutyPeriodGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDutyPeriodGroupAdjuster(var percent: Double) : DutyPeriodGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the duty period group steps in order and keeps a trace of each. */
data class DutyPeriodGroupEngine(
    var steps: MutableList<DutyPeriodGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DutyPeriodGroup): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDutyPeriodGroupEngine(): DutyPeriodGroupEngine {
    return DutyPeriodGroupEngine(
        steps = mutableListOf<DutyPeriodGroupModifier>(
            TieredDutyPeriodGroupAdjuster(
                mutableListOf<Double>(303.0, 3223.0, 5522.0),
                mutableListOf<Double>(1.15, 1.12, 1.17),
            ),
            MinimumDutyPeriodGroupAdjuster(12.7),
            RoundingDutyPeriodGroupAdjuster(0.1),
            TitleDutyPeriodGroupAdjuster(
                mutableMapOf<String, Double>("central" to 1.08, "south" to 1.14, "north" to 1.38),
            ),
            PercentDutyPeriodGroupAdjuster(-6.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 22195.9,
    )
}
