package com.acme.transport.safety.coaching.coachingscore

/** One step of the coaching entry calculation. */
interface CoachingEntryAdjuster {
    fun describe(): String
    fun adjust(item: CoachingEntry, value: Double): Double
}

data class CoachingEntryAmountOffsetAdjuster(var weight: Double) : CoachingEntryAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: CoachingEntry, value: Double): Double = value + item.amount * weight
}

data class ChannelCoachingEntryAdjuster(var factors: MutableMap<String, Double>) : CoachingEntryAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: CoachingEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCoachingEntryAdjuster(var cap: Double) : CoachingEntryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CoachingEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the coaching entry steps in order and keeps a trace of each. */
data class CoachingEntryEngine(
    var steps: MutableList<CoachingEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CoachingEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingEntryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CoachingEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCoachingEntryEngine(): CoachingEntryEngine {
    return CoachingEntryEngine(
        steps = mutableListOf<CoachingEntryAdjuster>(
            CoachingEntryAmountOffsetAdjuster(1.33),
            ChannelCoachingEntryAdjuster(mutableMapOf<String, Double>("west" to 0.85)),
            CapCoachingEntryAdjuster(20638.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 35837.8,
    )
}
