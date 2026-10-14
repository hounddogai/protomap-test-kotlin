package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dispute adjustment calculation. */
interface DisputeAdjustmentFactor {
    fun describe(): String
    fun adjust(item: DisputeAdjustment, value: Double): Double
}

data class CapDisputeAdjustmentAdjuster(var cap: Double) : DisputeAdjustmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputeAdjustment, value: Double): Double = minOf(value, cap)
}

data class FlaggedDisputeAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeAdjustmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DisputeAdjustmentCeilingOffsetAdjuster(var weight: Double) : DisputeAdjustmentFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: DisputeAdjustment, value: Double): Double = value + item.ceiling * weight
}

data class MinimumDisputeAdjustmentAdjuster(var minimum: Double) : DisputeAdjustmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dispute adjustment steps in order and keeps a trace of each. */
data class DisputeAdjustmentEngine(
    var steps: MutableList<DisputeAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DisputeAdjustment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DisputeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDisputeAdjustmentEngine(flags: Registry): DisputeAdjustmentEngine {
    return DisputeAdjustmentEngine(
        steps = mutableListOf<DisputeAdjustmentFactor>(
            CapDisputeAdjustmentAdjuster(21046.0),
            FlaggedDisputeAdjustmentAdjuster(flags, "dispute-adjustment-scheduled", -1.9),
            DisputeAdjustmentCeilingOffsetAdjuster(1.98),
            MinimumDisputeAdjustmentAdjuster(42.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 92768.3,
    )
}
