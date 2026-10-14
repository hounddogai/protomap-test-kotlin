package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dispute rollup calculation. */
interface DisputeRollupAdjuster {
    fun describe(): String
    fun adjust(item: DisputeRollup, value: Double): Double
}

data class FlaggedDisputeRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDisputeRollupAdjuster(var minimum: Double) : DisputeRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DisputeRollupCeilingOffsetAdjuster(var weight: Double) : DisputeRollupAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: DisputeRollup, value: Double): Double = value + item.ceiling * weight
}

data class CapDisputeRollupAdjuster(var cap: Double) : DisputeRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputeRollup, value: Double): Double = minOf(value, cap)
}

data class PercentDisputeRollupAdjuster(var percent: Double) : DisputeRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionDisputeRollupAdjuster(var factors: MutableMap<String, Double>) : DisputeRollupAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: DisputeRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the dispute rollup steps in order and keeps a trace of each. */
data class DisputeRollupEngine(
    var steps: MutableList<DisputeRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DisputeRollup): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DisputeRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDisputeRollupEngine(flags: Registry): DisputeRollupEngine {
    return DisputeRollupEngine(
        steps = mutableListOf<DisputeRollupAdjuster>(
            FlaggedDisputeRollupAdjuster(flags, "dispute-rollup-adaptive", -7.9),
            MinimumDisputeRollupAdjuster(33.5),
            DisputeRollupCeilingOffsetAdjuster(0.85),
            CapDisputeRollupAdjuster(11841.0),
            PercentDisputeRollupAdjuster(-4.3),
            RegionDisputeRollupAdjuster(mutableMapOf<String, Double>("central" to 1.28)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 66394.6,
    )
}
