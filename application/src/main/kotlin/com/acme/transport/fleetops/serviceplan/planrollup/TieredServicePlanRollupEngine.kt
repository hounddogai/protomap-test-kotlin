package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered service plan rollup calculation. */
interface TieredServicePlanRollupAdjuster {
    fun describe(): String
    fun adjust(item: TieredServicePlanRollup, value: Double): Double
}

data class TieredTieredServicePlanRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredServicePlanRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredServicePlanRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTieredServicePlanRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredServicePlanRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredServicePlanRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredServicePlanRollupSequenceOffsetAdjuster(var weight: Double) : TieredServicePlanRollupAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: TieredServicePlanRollup, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class RoundingTieredServicePlanRollupAdjuster(var step: Double) : TieredServicePlanRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredServicePlanRollup, value: Double): Double = roundTo(value, step)
}

data class PercentTieredServicePlanRollupAdjuster(var percent: Double) : TieredServicePlanRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredServicePlanRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered service plan rollup steps in order and keeps a trace of each. */
data class TieredServicePlanRollupEngine(
    var steps: MutableList<TieredServicePlanRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredServicePlanRollup): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredServicePlanRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredServicePlanRollupEngine(flags: Registry): TieredServicePlanRollupEngine {
    return TieredServicePlanRollupEngine(
        steps = mutableListOf<TieredServicePlanRollupAdjuster>(
            TieredTieredServicePlanRollupAdjuster(
                mutableListOf<Double>(3719.0, 9386.0),
                mutableListOf<Double>(1.09, 0.96),
            ),
            FlaggedTieredServicePlanRollupAdjuster(flags, "tiered-service-plan-rollup-global", 3.5),
            TieredServicePlanRollupSequenceOffsetAdjuster(0.52),
            RoundingTieredServicePlanRollupAdjuster(0.25),
            PercentTieredServicePlanRollupAdjuster(-13.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 73827.2,
    )
}
