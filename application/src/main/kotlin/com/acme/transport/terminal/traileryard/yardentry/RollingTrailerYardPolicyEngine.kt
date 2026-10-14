package com.acme.transport.terminal.traileryard.yardentry

/** One step of the rolling trailer yard policy calculation. */
interface RollingTrailerYardPolicyFactor {
    fun describe(): String
    fun adjust(item: RollingTrailerYardPolicy, value: Double): Double
}

data class TitleRollingTrailerYardPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingTrailerYardPolicyFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingTrailerYardPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingTrailerYardPolicyAdjuster(var percent: Double) : RollingTrailerYardPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingTrailerYardPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingTrailerYardPolicyAdjuster(var cap: Double) : RollingTrailerYardPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTrailerYardPolicy, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingTrailerYardPolicyAdjuster(var minimum: Double) : RollingTrailerYardPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingTrailerYardPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling trailer yard policy steps in order and keeps a trace of each. */
data class RollingTrailerYardPolicyEngine(
    var steps: MutableList<RollingTrailerYardPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingTrailerYardPolicy): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingTrailerYardPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingTrailerYardPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingTrailerYardPolicyEngine(): RollingTrailerYardPolicyEngine {
    return RollingTrailerYardPolicyEngine(
        steps = mutableListOf<RollingTrailerYardPolicyFactor>(
            TitleRollingTrailerYardPolicyAdjuster(mutableMapOf<String, Double>("north" to 0.89)),
            PercentRollingTrailerYardPolicyAdjuster(-23.9),
            CapRollingTrailerYardPolicyAdjuster(45667.0),
            MinimumRollingTrailerYardPolicyAdjuster(2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 43466.9,
    )
}
