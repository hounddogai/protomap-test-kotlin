package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered service plan allocation calculation. */
interface TieredServicePlanAllocationModifier {
    fun describe(): String
    fun adjust(item: TieredServicePlanAllocation, value: Double): Double
}

data class TieredTieredServicePlanAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredServicePlanAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredServicePlanAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTieredServicePlanAllocationAdjuster(
    var minimum: Double,
) : TieredServicePlanAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredServicePlanAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTieredServicePlanAllocationAdjuster(var cap: Double) : TieredServicePlanAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredServicePlanAllocation, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredServicePlanAllocationAdjuster(var step: Double) : TieredServicePlanAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredServicePlanAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedTieredServicePlanAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredServicePlanAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredServicePlanAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTieredServicePlanAllocationAdjuster(
    var percent: Double,
) : TieredServicePlanAllocationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredServicePlanAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered service plan allocation steps in order and keeps a trace of each. */
data class TieredServicePlanAllocationEngine(
    var steps: MutableList<TieredServicePlanAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredServicePlanAllocation): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredServicePlanAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredServicePlanAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredServicePlanAllocationEngine(flags: Registry): TieredServicePlanAllocationEngine {
    return TieredServicePlanAllocationEngine(
        steps = mutableListOf<TieredServicePlanAllocationModifier>(
            TieredTieredServicePlanAllocationAdjuster(
                mutableListOf<Double>(1353.0, 3874.0, 7540.0, 9030.0),
                mutableListOf<Double>(1.12, 0.98, 1.0, 1.15),
            ),
            MinimumTieredServicePlanAllocationAdjuster(30.1),
            CapTieredServicePlanAllocationAdjuster(35655.0),
            RoundingTieredServicePlanAllocationAdjuster(5.0),
            FlaggedTieredServicePlanAllocationAdjuster(flags, "tiered-service-plan-allocation-strict", 3.3),
            PercentTieredServicePlanAllocationAdjuster(-4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 78938.3,
    )
}
