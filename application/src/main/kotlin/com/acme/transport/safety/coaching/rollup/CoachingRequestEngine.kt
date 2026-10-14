package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the coaching request calculation. */
interface CoachingRequestFactor {
    fun describe(): String
    fun adjust(item: CoachingRequest, value: Double): Double
}

data class RoundingCoachingRequestAdjuster(var step: Double) : CoachingRequestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingRequest, value: Double): Double = roundTo(value, step)
}

data class FlaggedCoachingRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCoachingRequestAdjuster(var percent: Double) : CoachingRequestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelCoachingRequestAdjuster(var factors: MutableMap<String, Double>) : CoachingRequestFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: CoachingRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CoachingRequestBudgetOffsetAdjuster(var weight: Double) : CoachingRequestFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: CoachingRequest, value: Double): Double = value + item.budget * weight
}

/** Runs the coaching request steps in order and keeps a trace of each. */
data class CoachingRequestEngine(
    var steps: MutableList<CoachingRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CoachingRequest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CoachingRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCoachingRequestEngine(flags: Registry): CoachingRequestEngine {
    return CoachingRequestEngine(
        steps = mutableListOf<CoachingRequestFactor>(
            RoundingCoachingRequestAdjuster(1.0),
            FlaggedCoachingRequestAdjuster(flags, "coaching-request-pending", -2.6),
            PercentCoachingRequestAdjuster(-3.2),
            ChannelCoachingRequestAdjuster(mutableMapOf<String, Double>("west" to 1.24)),
            CoachingRequestBudgetOffsetAdjuster(1.93),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 77893.9,
    )
}
