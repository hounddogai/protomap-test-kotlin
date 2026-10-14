package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the duty period entry calculation. */
interface DutyPeriodEntryStep {
    fun describe(): String
    fun adjust(item: DutyPeriodEntry, value: Double): Double
}

data class MinimumDutyPeriodEntryAdjuster(var minimum: Double) : DutyPeriodEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelDutyPeriodEntryAdjuster(var factors: MutableMap<String, Double>) : DutyPeriodEntryStep {
    override fun describe(): String = "by label"

    override fun adjust(item: DutyPeriodEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDutyPeriodEntryAdjuster(var step: Double) : DutyPeriodEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the duty period entry steps in order and keeps a trace of each. */
data class DutyPeriodEntryEngine(
    var steps: MutableList<DutyPeriodEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DutyPeriodEntry): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDutyPeriodEntryEngine(): DutyPeriodEntryEngine {
    return DutyPeriodEntryEngine(
        steps = mutableListOf<DutyPeriodEntryStep>(
            MinimumDutyPeriodEntryAdjuster(14.9),
            LabelDutyPeriodEntryAdjuster(mutableMapOf<String, Double>("retail" to 0.9, "south" to 1.3)),
            RoundingDutyPeriodEntryAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 82381.0,
    )
}
