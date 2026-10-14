package com.acme.transport.linehaul.backhaul.assignment

/** One step of the backhaul policy calculation. */
interface BackhaulPolicyAdjuster {
    fun describe(): String
    fun adjust(item: BackhaulPolicy, value: Double): Double
}

data class BackhaulPolicySequenceOffsetAdjuster(var weight: Double) : BackhaulPolicyAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: BackhaulPolicy, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class DepotBackhaulPolicyAdjuster(var factors: MutableMap<String, Double>) : BackhaulPolicyAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: BackhaulPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBackhaulPolicyAdjuster(var minimum: Double) : BackhaulPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the backhaul policy steps in order and keeps a trace of each. */
data class BackhaulPolicyEngine(
    var steps: MutableList<BackhaulPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulPolicy): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BackhaulPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulPolicyEngine(): BackhaulPolicyEngine {
    return BackhaulPolicyEngine(
        steps = mutableListOf<BackhaulPolicyAdjuster>(
            BackhaulPolicySequenceOffsetAdjuster(0.29),
            DepotBackhaulPolicyAdjuster(mutableMapOf<String, Double>("north" to 0.71, "central" to 0.75)),
            MinimumBackhaulPolicyAdjuster(43.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 27318.5,
    )
}
