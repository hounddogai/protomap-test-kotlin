package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dispute assignment calculation. */
interface DisputeAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: DisputeAssignment, value: Double): Double
}

data class RoundingDisputeAssignmentAdjuster(var step: Double) : DisputeAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeAssignment, value: Double): Double = roundTo(value, step)
}

data class TieredDisputeAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDisputeAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryDisputeAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : DisputeAssignmentAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: DisputeAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDisputeAssignmentAdjuster(var minimum: Double) : DisputeAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dispute assignment steps in order and keeps a trace of each. */
data class DisputeAssignmentEngine(
    var steps: MutableList<DisputeAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DisputeAssignment): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DisputeAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDisputeAssignmentEngine(flags: Registry): DisputeAssignmentEngine {
    return DisputeAssignmentEngine(
        steps = mutableListOf<DisputeAssignmentAdjuster>(
            RoundingDisputeAssignmentAdjuster(0.25),
            TieredDisputeAssignmentAdjuster(
                mutableListOf<Double>(489.0, 724.0, 5566.0, 7728.0),
                mutableListOf<Double>(0.89, 1.01, 0.89, 1.15),
            ),
            FlaggedDisputeAssignmentAdjuster(flags, "dispute-assignment-regional", -1.1),
            CountryDisputeAssignmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.22, "south" to 0.79, "north" to 1.16),
            ),
            MinimumDisputeAssignmentAdjuster(28.8),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 9345.2,
    )
}
