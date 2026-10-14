package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped dock door template calculation. */
interface CappedDockDoorTemplateAdjuster {
    fun describe(): String
    fun adjust(item: CappedDockDoorTemplate, value: Double): Double
}

data class RoundingCappedDockDoorTemplateAdjuster(var step: Double) : CappedDockDoorTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedDockDoorTemplate, value: Double): Double = roundTo(value, step)
}

data class TieredCappedDockDoorTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedDockDoorTemplateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedDockDoorTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedDockDoorTemplateAdjuster(var cap: Double) : CappedDockDoorTemplateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedDockDoorTemplate, value: Double): Double = minOf(value, cap)
}

data class ChannelCappedDockDoorTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedDockDoorTemplateAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: CappedDockDoorTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CappedDockDoorTemplateThresholdOffsetAdjuster(var weight: Double) : CappedDockDoorTemplateAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: CappedDockDoorTemplate, value: Double): Double = value + item.threshold * weight
}

/** Runs the capped dock door template steps in order and keeps a trace of each. */
data class CappedDockDoorTemplateEngine(
    var steps: MutableList<CappedDockDoorTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedDockDoorTemplate): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedDockDoorTemplateAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedDockDoorTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedDockDoorTemplateEngine(): CappedDockDoorTemplateEngine {
    return CappedDockDoorTemplateEngine(
        steps = mutableListOf<CappedDockDoorTemplateAdjuster>(
            RoundingCappedDockDoorTemplateAdjuster(1.0),
            TieredCappedDockDoorTemplateAdjuster(
                mutableListOf<Double>(5231.0, 8813.0, 9876.0),
                mutableListOf<Double>(1.01, 1.13, 1.17),
            ),
            CapCappedDockDoorTemplateAdjuster(35615.0),
            ChannelCappedDockDoorTemplateAdjuster(
                mutableMapOf<String, Double>("north" to 1.33, "central" to 1.07, "west" to 1.22),
            ),
            CappedDockDoorTemplateThresholdOffsetAdjuster(0.85),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 96305.0,
    )
}
