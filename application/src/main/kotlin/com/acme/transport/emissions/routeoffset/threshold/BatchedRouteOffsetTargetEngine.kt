package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched route offset target calculation. */
interface BatchedRouteOffsetTargetAdjuster {
    fun describe(): String
    fun adjust(item: BatchedRouteOffsetTarget, value: Double): Double
}

data class FlaggedBatchedRouteOffsetTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRouteOffsetTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRouteOffsetTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedRouteOffsetTargetMaximumOffsetAdjuster(
    var weight: Double,
) : BatchedRouteOffsetTargetAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: BatchedRouteOffsetTarget, value: Double): Double = value + item.maximum * weight
}

data class TieredBatchedRouteOffsetTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRouteOffsetTargetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRouteOffsetTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelBatchedRouteOffsetTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedRouteOffsetTargetAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedRouteOffsetTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched route offset target steps in order and keeps a trace of each. */
data class BatchedRouteOffsetTargetEngine(
    var steps: MutableList<BatchedRouteOffsetTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedRouteOffsetTarget): Double {
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

    fun evaluateAll(items: MutableList<BatchedRouteOffsetTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedRouteOffsetTargetEngine(flags: Registry): BatchedRouteOffsetTargetEngine {
    return BatchedRouteOffsetTargetEngine(
        steps = mutableListOf<BatchedRouteOffsetTargetAdjuster>(
            FlaggedBatchedRouteOffsetTargetAdjuster(flags, "batched-route-offset-target-lenient", 0.4),
            BatchedRouteOffsetTargetMaximumOffsetAdjuster(0.52),
            TieredBatchedRouteOffsetTargetAdjuster(
                mutableListOf<Double>(3224.0, 7010.0),
                mutableListOf<Double>(1.2, 0.83),
            ),
            LabelBatchedRouteOffsetTargetAdjuster(mutableMapOf<String, Double>("central" to 1.03)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 87707.1,
    )
}
