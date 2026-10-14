package com.acme.transport.customs.clearance.clearanceestimate

/** One step of the pending clearance override calculation. */
interface PendingClearanceOverrideAdjuster {
    fun describe(): String
    fun adjust(item: PendingClearanceOverride, value: Double): Double
}

data class PendingClearanceOverrideDiscountOffsetAdjuster(
    var weight: Double,
) : PendingClearanceOverrideAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: PendingClearanceOverride, value: Double): Double = value + item.discount * weight
}

data class TieredPendingClearanceOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingClearanceOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingClearanceOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPendingClearanceOverrideAdjuster(var percent: Double) : PendingClearanceOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingClearanceOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LanePendingClearanceOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingClearanceOverrideAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: PendingClearanceOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending clearance override steps in order and keeps a trace of each. */
data class PendingClearanceOverrideEngine(
    var steps: MutableList<PendingClearanceOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingClearanceOverride): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingClearanceOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingClearanceOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingClearanceOverrideEngine(): PendingClearanceOverrideEngine {
    return PendingClearanceOverrideEngine(
        steps = mutableListOf<PendingClearanceOverrideAdjuster>(
            PendingClearanceOverrideDiscountOffsetAdjuster(0.25),
            TieredPendingClearanceOverrideAdjuster(
                mutableListOf<Double>(1921.0, 3323.0, 6045.0, 6855.0),
                mutableListOf<Double>(0.98, 0.96, 1.1, 1.01),
            ),
            PercentPendingClearanceOverrideAdjuster(-13.1),
            LanePendingClearanceOverrideAdjuster(mutableMapOf<String, Double>("east" to 1.22)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 10018.0,
    )
}
