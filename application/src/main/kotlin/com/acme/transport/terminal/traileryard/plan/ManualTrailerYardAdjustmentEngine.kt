package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual trailer yard adjustment calculation. */
interface ManualTrailerYardAdjustmentStep {
    fun describe(): String
    fun adjust(item: ManualTrailerYardAdjustment, value: Double): Double
}

data class NameManualTrailerYardAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualTrailerYardAdjustmentStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualTrailerYardAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualTrailerYardAdjustmentAdjuster(var percent: Double) : ManualTrailerYardAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTrailerYardAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualTrailerYardAdjustmentAdjuster(var step: Double) : ManualTrailerYardAdjustmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTrailerYardAdjustment, value: Double): Double = roundTo(value, step)
}

data class ManualTrailerYardAdjustmentStartsAtOffsetAdjuster(
    var weight: Double,
) : ManualTrailerYardAdjustmentStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: ManualTrailerYardAdjustment, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

/** Runs the manual trailer yard adjustment steps in order and keeps a trace of each. */
data class ManualTrailerYardAdjustmentEngine(
    var steps: MutableList<ManualTrailerYardAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualTrailerYardAdjustment): Double {
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

    fun evaluateAll(items: MutableList<ManualTrailerYardAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualTrailerYardAdjustmentEngine(): ManualTrailerYardAdjustmentEngine {
    return ManualTrailerYardAdjustmentEngine(
        steps = mutableListOf<ManualTrailerYardAdjustmentStep>(
            NameManualTrailerYardAdjustmentAdjuster(mutableMapOf<String, Double>("north" to 1.08)),
            PercentManualTrailerYardAdjustmentAdjuster(-6.5),
            RoundingManualTrailerYardAdjustmentAdjuster(1.0),
            ManualTrailerYardAdjustmentStartsAtOffsetAdjuster(0.27),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 46744.5,
    )
}
