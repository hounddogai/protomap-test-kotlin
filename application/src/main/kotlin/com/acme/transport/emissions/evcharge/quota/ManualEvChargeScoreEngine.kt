package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual ev charge score calculation. */
interface ManualEvChargeScoreAdjuster {
    fun describe(): String
    fun adjust(item: ManualEvChargeScore, value: Double): Double
}

data class NameManualEvChargeScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualEvChargeScoreAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualEvChargeScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualEvChargeScoreMaximumOffsetAdjuster(var weight: Double) : ManualEvChargeScoreAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ManualEvChargeScore, value: Double): Double = value + item.maximum * weight
}

data class PercentManualEvChargeScoreAdjuster(var percent: Double) : ManualEvChargeScoreAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualEvChargeScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualEvChargeScoreAdjuster(var step: Double) : ManualEvChargeScoreAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualEvChargeScore, value: Double): Double = roundTo(value, step)
}

data class MinimumManualEvChargeScoreAdjuster(var minimum: Double) : ManualEvChargeScoreAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualEvChargeScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual ev charge score steps in order and keeps a trace of each. */
data class ManualEvChargeScoreEngine(
    var steps: MutableList<ManualEvChargeScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualEvChargeScore): Double {
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

    fun evaluateAll(items: MutableList<ManualEvChargeScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualEvChargeScoreEngine(): ManualEvChargeScoreEngine {
    return ManualEvChargeScoreEngine(
        steps = mutableListOf<ManualEvChargeScoreAdjuster>(
            NameManualEvChargeScoreAdjuster(mutableMapOf<String, Double>("north" to 1.08, "east" to 1.01)),
            ManualEvChargeScoreMaximumOffsetAdjuster(1.68),
            PercentManualEvChargeScoreAdjuster(21.5),
            RoundingManualEvChargeScoreAdjuster(0.1),
            MinimumManualEvChargeScoreAdjuster(33.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 50231.0,
    )
}
