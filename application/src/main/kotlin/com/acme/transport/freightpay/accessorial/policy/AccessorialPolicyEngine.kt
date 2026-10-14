package com.acme.transport.freightpay.accessorial.policy

/** One step of the accessorial policy calculation. */
interface AccessorialPolicyAdjuster {
    fun describe(): String
    fun adjust(item: AccessorialPolicy, value: Double): Double
}

data class PercentAccessorialPolicyAdjuster(var percent: Double) : AccessorialPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAccessorialPolicyAdjuster(var cap: Double) : AccessorialPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialPolicy, value: Double): Double = minOf(value, cap)
}

data class MinimumAccessorialPolicyAdjuster(var minimum: Double) : AccessorialPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the accessorial policy steps in order and keeps a trace of each. */
data class AccessorialPolicyEngine(
    var steps: MutableList<AccessorialPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialPolicy): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AccessorialPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AccessorialPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialPolicyEngine(): AccessorialPolicyEngine {
    return AccessorialPolicyEngine(
        steps = mutableListOf<AccessorialPolicyAdjuster>(
            PercentAccessorialPolicyAdjuster(0.6),
            CapAccessorialPolicyAdjuster(3130.0),
            MinimumAccessorialPolicyAdjuster(45.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 69396.6,
    )
}
