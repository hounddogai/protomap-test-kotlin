package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.flags.Registry

/** One step of the strict dispute summary calculation. */
interface StrictDisputeSummaryAdjuster {
    fun describe(): String
    fun adjust(item: StrictDisputeSummary, value: Double): Double
}

data class LaneStrictDisputeSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictDisputeSummaryAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: StrictDisputeSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictDisputeSummaryAdjuster(var cap: Double) : StrictDisputeSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictDisputeSummary, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictDisputeSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictDisputeSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictDisputeSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict dispute summary steps in order and keeps a trace of each. */
data class StrictDisputeSummaryEngine(
    var steps: MutableList<StrictDisputeSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictDisputeSummary): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictDisputeSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictDisputeSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictDisputeSummaryEngine(flags: Registry): StrictDisputeSummaryEngine {
    return StrictDisputeSummaryEngine(
        steps = mutableListOf<StrictDisputeSummaryAdjuster>(
            LaneStrictDisputeSummaryAdjuster(
                mutableMapOf<String, Double>("west" to 1.25, "east" to 0.95, "central" to 1.26),
            ),
            CapStrictDisputeSummaryAdjuster(29144.0),
            FlaggedStrictDisputeSummaryAdjuster(flags, "strict-dispute-summary-weighted", -7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 66030.6,
    )
}
