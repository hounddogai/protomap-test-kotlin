package com.acme.transport.lastmile.redelivery.window

/** One step of the pending redelivery window calculation. */
interface PendingRedeliveryWindowAdjuster {
    fun describe(): String
    fun adjust(item: PendingRedeliveryWindow, value: Double): Double
}

data class PendingRedeliveryWindowAmountOffsetAdjuster(var weight: Double) : PendingRedeliveryWindowAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: PendingRedeliveryWindow, value: Double): Double = value + item.amount * weight
}

data class PercentPendingRedeliveryWindowAdjuster(var percent: Double) : PendingRedeliveryWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingRedeliveryWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingRedeliveryWindowAdjuster(var cap: Double) : PendingRedeliveryWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingRedeliveryWindow, value: Double): Double = minOf(value, cap)
}

data class SourcePendingRedeliveryWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRedeliveryWindowAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: PendingRedeliveryWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending redelivery window steps in order and keeps a trace of each. */
data class PendingRedeliveryWindowEngine(
    var steps: MutableList<PendingRedeliveryWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingRedeliveryWindow): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingRedeliveryWindowAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingRedeliveryWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingRedeliveryWindowEngine(): PendingRedeliveryWindowEngine {
    return PendingRedeliveryWindowEngine(
        steps = mutableListOf<PendingRedeliveryWindowAdjuster>(
            PendingRedeliveryWindowAmountOffsetAdjuster(0.3),
            PercentPendingRedeliveryWindowAdjuster(-24.9),
            CapPendingRedeliveryWindowAdjuster(35107.0),
            SourcePendingRedeliveryWindowAdjuster(
                mutableMapOf<String, Double>("central" to 0.8, "south" to 1.14, "north" to 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 69631.4,
    )
}
