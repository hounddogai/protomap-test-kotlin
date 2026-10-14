package com.acme.transport.lastmile.redelivery.overrideitem

import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery override calculation. */
interface RedeliveryOverrideFactor {
    fun describe(): String
    fun adjust(item: RedeliveryOverride, value: Double): Double
}

data class PercentRedeliveryOverrideAdjuster(var percent: Double) : RedeliveryOverrideFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceRedeliveryOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : RedeliveryOverrideFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: RedeliveryOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRedeliveryOverrideAdjuster(var minimum: Double) : RedeliveryOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRedeliveryOverrideAdjuster(var cap: Double) : RedeliveryOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryOverride, value: Double): Double = minOf(value, cap)
}

data class RoundingRedeliveryOverrideAdjuster(var step: Double) : RedeliveryOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the redelivery override steps in order and keeps a trace of each. */
data class RedeliveryOverrideEngine(
    var steps: MutableList<RedeliveryOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RedeliveryOverride): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRedeliveryOverrideEngine(): RedeliveryOverrideEngine {
    return RedeliveryOverrideEngine(
        steps = mutableListOf<RedeliveryOverrideFactor>(
            PercentRedeliveryOverrideAdjuster(-10.9),
            SourceRedeliveryOverrideAdjuster(mutableMapOf<String, Double>("retail" to 1.37, "west" to 0.71)),
            MinimumRedeliveryOverrideAdjuster(6.5),
            CapRedeliveryOverrideAdjuster(4469.0),
            RoundingRedeliveryOverrideAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 32853.0,
    )
}
