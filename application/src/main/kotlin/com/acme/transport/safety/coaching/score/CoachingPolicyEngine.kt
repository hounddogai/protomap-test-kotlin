package com.acme.transport.safety.coaching.score

/** One step of the coaching policy calculation. */
interface CoachingPolicyAdjuster {
    fun describe(): String
    fun adjust(item: CoachingPolicy, value: Double): Double
}

data class TieredCoachingPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCoachingPolicyAdjuster(var percent: Double) : CoachingPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CoachingPolicyFuelLitersOffsetAdjuster(var weight: Double) : CoachingPolicyAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: CoachingPolicy, value: Double): Double = value + item.fuelLiters * weight
}

data class CapCoachingPolicyAdjuster(var cap: Double) : CoachingPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CoachingPolicy, value: Double): Double = minOf(value, cap)
}

/** Runs the coaching policy steps in order and keeps a trace of each. */
data class CoachingPolicyEngine(
    var steps: MutableList<CoachingPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CoachingPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CoachingPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCoachingPolicyEngine(): CoachingPolicyEngine {
    return CoachingPolicyEngine(
        steps = mutableListOf<CoachingPolicyAdjuster>(
            TieredCoachingPolicyAdjuster(
                mutableListOf<Double>(577.0, 1204.0, 9153.0, 9563.0),
                mutableListOf<Double>(1.17, 0.91, 0.84, 1.07),
            ),
            PercentCoachingPolicyAdjuster(-4.7),
            CoachingPolicyFuelLitersOffsetAdjuster(1.78),
            CapCoachingPolicyAdjuster(26559.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 39075.0,
    )
}
