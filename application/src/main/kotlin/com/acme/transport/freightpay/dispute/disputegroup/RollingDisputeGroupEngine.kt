package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling dispute group calculation. */
interface RollingDisputeGroupAdjuster {
    fun describe(): String
    fun adjust(item: RollingDisputeGroup, value: Double): Double
}

data class TitleRollingDisputeGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingDisputeGroupAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingDisputeGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingDisputeGroupExpiresAtOffsetAdjuster(var weight: Double) : RollingDisputeGroupAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: RollingDisputeGroup, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class FlaggedRollingDisputeGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDisputeGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDisputeGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingDisputeGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDisputeGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDisputeGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingDisputeGroupAdjuster(var cap: Double) : RollingDisputeGroupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDisputeGroup, value: Double): Double = minOf(value, cap)
}

data class RoundingRollingDisputeGroupAdjuster(var step: Double) : RollingDisputeGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDisputeGroup, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling dispute group steps in order and keeps a trace of each. */
data class RollingDisputeGroupEngine(
    var steps: MutableList<RollingDisputeGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingDisputeGroup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingDisputeGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingDisputeGroupEngine(flags: Registry): RollingDisputeGroupEngine {
    return RollingDisputeGroupEngine(
        steps = mutableListOf<RollingDisputeGroupAdjuster>(
            TitleRollingDisputeGroupAdjuster(
                mutableMapOf<String, Double>("retail" to 1.31, "online" to 0.96, "south" to 0.99),
            ),
            RollingDisputeGroupExpiresAtOffsetAdjuster(0.27),
            FlaggedRollingDisputeGroupAdjuster(flags, "rolling-dispute-group-scheduled", -9.9),
            TieredRollingDisputeGroupAdjuster(
                mutableListOf<Double>(4140.0, 8964.0),
                mutableListOf<Double>(0.89, 1.16),
            ),
            CapRollingDisputeGroupAdjuster(33488.0),
            RoundingRollingDisputeGroupAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 24350.1,
    )
}
