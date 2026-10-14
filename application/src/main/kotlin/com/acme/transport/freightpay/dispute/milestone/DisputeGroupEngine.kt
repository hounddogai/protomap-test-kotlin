package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dispute group calculation. */
interface DisputeGroupAdjuster {
    fun describe(): String
    fun adjust(item: DisputeGroup, value: Double): Double
}

data class NameDisputeGroupAdjuster(var factors: MutableMap<String, Double>) : DisputeGroupAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: DisputeGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDisputeGroupAdjuster(var percent: Double) : DisputeGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDisputeGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDisputeGroupAdjuster(var step: Double) : DisputeGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeGroup, value: Double): Double = roundTo(value, step)
}

/** Runs the dispute group steps in order and keeps a trace of each. */
data class DisputeGroupEngine(
    var steps: MutableList<DisputeGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DisputeGroup): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeGroupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDisputeGroupEngine(flags: Registry): DisputeGroupEngine {
    return DisputeGroupEngine(
        steps = mutableListOf<DisputeGroupAdjuster>(
            NameDisputeGroupAdjuster(mutableMapOf<String, Double>("west" to 1.21)),
            PercentDisputeGroupAdjuster(-5.8),
            FlaggedDisputeGroupAdjuster(flags, "dispute-group-default", -6.4),
            RoundingDisputeGroupAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 88788.7,
    )
}
