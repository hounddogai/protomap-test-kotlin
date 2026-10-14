package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted dock door audit calculation. */
interface WeightedDockDoorAuditFactor {
    fun describe(): String
    fun adjust(item: WeightedDockDoorAudit, value: Double): Double
}

data class RoundingWeightedDockDoorAuditAdjuster(var step: Double) : WeightedDockDoorAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedDockDoorAudit, value: Double): Double = roundTo(value, step)
}

data class ChannelWeightedDockDoorAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDockDoorAuditFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: WeightedDockDoorAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentWeightedDockDoorAuditAdjuster(var percent: Double) : WeightedDockDoorAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedDockDoorAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedDockDoorAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedDockDoorAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedDockDoorAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapWeightedDockDoorAuditAdjuster(var cap: Double) : WeightedDockDoorAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedDockDoorAudit, value: Double): Double = minOf(value, cap)
}

data class MinimumWeightedDockDoorAuditAdjuster(var minimum: Double) : WeightedDockDoorAuditFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedDockDoorAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted dock door audit steps in order and keeps a trace of each. */
data class WeightedDockDoorAuditEngine(
    var steps: MutableList<WeightedDockDoorAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedDockDoorAudit): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedDockDoorAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedDockDoorAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedDockDoorAuditEngine(): WeightedDockDoorAuditEngine {
    return WeightedDockDoorAuditEngine(
        steps = mutableListOf<WeightedDockDoorAuditFactor>(
            RoundingWeightedDockDoorAuditAdjuster(1.0),
            ChannelWeightedDockDoorAuditAdjuster(
                mutableMapOf<String, Double>("north" to 0.88, "central" to 1.09, "south" to 1.04),
            ),
            PercentWeightedDockDoorAuditAdjuster(-21.9),
            TieredWeightedDockDoorAuditAdjuster(
                mutableListOf<Double>(4344.0, 9423.0),
                mutableListOf<Double>(1.12, 0.81),
            ),
            CapWeightedDockDoorAuditAdjuster(22881.0),
            MinimumWeightedDockDoorAuditAdjuster(22.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 35988.9,
    )
}
