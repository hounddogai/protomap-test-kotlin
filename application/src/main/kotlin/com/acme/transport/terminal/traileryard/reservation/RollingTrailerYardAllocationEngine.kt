package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling trailer yard allocation calculation. */
interface RollingTrailerYardAllocationModifier {
    fun describe(): String
    fun adjust(item: RollingTrailerYardAllocation, value: Double): Double
}

data class LaneRollingTrailerYardAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingTrailerYardAllocationModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: RollingTrailerYardAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRollingTrailerYardAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingTrailerYardAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingTrailerYardAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingTrailerYardAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingTrailerYardAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingTrailerYardAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RollingTrailerYardAllocationRatioOffsetAdjuster(
    var weight: Double,
) : RollingTrailerYardAllocationModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: RollingTrailerYardAllocation, value: Double): Double = value + item.ratio * weight
}

data class PercentRollingTrailerYardAllocationAdjuster(
    var percent: Double,
) : RollingTrailerYardAllocationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingTrailerYardAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingTrailerYardAllocationAdjuster(var cap: Double) : RollingTrailerYardAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTrailerYardAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling trailer yard allocation steps in order and keeps a trace of each. */
data class RollingTrailerYardAllocationEngine(
    var steps: MutableList<RollingTrailerYardAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingTrailerYardAllocation): Double {
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

    fun evaluateAll(items: MutableList<RollingTrailerYardAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingTrailerYardAllocationEngine(flags: Registry): RollingTrailerYardAllocationEngine {
    return RollingTrailerYardAllocationEngine(
        steps = mutableListOf<RollingTrailerYardAllocationModifier>(
            LaneRollingTrailerYardAllocationAdjuster(mutableMapOf<String, Double>("retail" to 0.84)),
            TieredRollingTrailerYardAllocationAdjuster(
                mutableListOf<Double>(1551.0, 7154.0, 8610.0),
                mutableListOf<Double>(0.89, 1.11, 1.03),
            ),
            FlaggedRollingTrailerYardAllocationAdjuster(flags, "rolling-trailer-yard-allocation-pending", -6.3),
            RollingTrailerYardAllocationRatioOffsetAdjuster(1.98),
            PercentRollingTrailerYardAllocationAdjuster(0.2),
            CapRollingTrailerYardAllocationAdjuster(1022.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 79481.6,
    )
}
