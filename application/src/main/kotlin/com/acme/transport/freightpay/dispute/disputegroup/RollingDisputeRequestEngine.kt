package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling dispute request calculation. */
interface RollingDisputeRequestModifier {
    fun describe(): String
    fun adjust(item: RollingDisputeRequest, value: Double): Double
}

data class RoundingRollingDisputeRequestAdjuster(var step: Double) : RollingDisputeRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDisputeRequest, value: Double): Double = roundTo(value, step)
}

data class LabelRollingDisputeRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingDisputeRequestModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RollingDisputeRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRollingDisputeRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDisputeRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDisputeRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRollingDisputeRequestAdjuster(var minimum: Double) : RollingDisputeRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDisputeRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRollingDisputeRequestAdjuster(var cap: Double) : RollingDisputeRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDisputeRequest, value: Double): Double = minOf(value, cap)
}

data class RollingDisputeRequestPalletsOffsetAdjuster(var weight: Double) : RollingDisputeRequestModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: RollingDisputeRequest, value: Double): Double = value + item.pallets.toDouble() * weight
}

/** Runs the rolling dispute request steps in order and keeps a trace of each. */
data class RollingDisputeRequestEngine(
    var steps: MutableList<RollingDisputeRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingDisputeRequest): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingDisputeRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingDisputeRequestEngine(): RollingDisputeRequestEngine {
    return RollingDisputeRequestEngine(
        steps = mutableListOf<RollingDisputeRequestModifier>(
            RoundingRollingDisputeRequestAdjuster(0.25),
            LabelRollingDisputeRequestAdjuster(
                mutableMapOf<String, Double>("central" to 1.24, "retail" to 1.31),
            ),
            TieredRollingDisputeRequestAdjuster(
                mutableListOf<Double>(909.0, 2090.0),
                mutableListOf<Double>(0.87, 0.9),
            ),
            MinimumRollingDisputeRequestAdjuster(2.8),
            CapRollingDisputeRequestAdjuster(43087.0),
            RollingDisputeRequestPalletsOffsetAdjuster(1.11),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 5453.3,
    )
}
