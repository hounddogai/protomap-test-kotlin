package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict rate card assignment calculation. */
interface StrictRateCardAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: StrictRateCardAssignment, value: Double): Double
}

data class FlaggedStrictRateCardAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRateCardAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRateCardAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictRateCardAssignmentAdjuster(var step: Double) : StrictRateCardAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRateCardAssignment, value: Double): Double = roundTo(value, step)
}

data class RegionStrictRateCardAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictRateCardAssignmentAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: StrictRateCardAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict rate card assignment steps in order and keeps a trace of each. */
data class StrictRateCardAssignmentEngine(
    var steps: MutableList<StrictRateCardAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictRateCardAssignment): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictRateCardAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictRateCardAssignmentEngine(flags: Registry): StrictRateCardAssignmentEngine {
    return StrictRateCardAssignmentEngine(
        steps = mutableListOf<StrictRateCardAssignmentAdjuster>(
            FlaggedStrictRateCardAssignmentAdjuster(flags, "strict-rate-card-assignment-rolling", -9.5),
            RoundingStrictRateCardAssignmentAdjuster(5.0),
            RegionStrictRateCardAssignmentAdjuster(mutableMapOf<String, Double>("east" to 0.83)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 2192.3,
    )
}
