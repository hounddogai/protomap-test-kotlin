package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled drop quota calculation. */
interface ScheduledDropQuotaFactor {
    fun describe(): String
    fun adjust(item: ScheduledDropQuota, value: Double): Double
}

data class PercentScheduledDropQuotaAdjuster(var percent: Double) : ScheduledDropQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledDropQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingScheduledDropQuotaAdjuster(var step: Double) : ScheduledDropQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledDropQuota, value: Double): Double = roundTo(value, step)
}

data class MinimumScheduledDropQuotaAdjuster(var minimum: Double) : ScheduledDropQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDropQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScheduledDropQuotaAdjuster(var cap: Double) : ScheduledDropQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDropQuota, value: Double): Double = minOf(value, cap)
}

data class RegionScheduledDropQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDropQuotaFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: ScheduledDropQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScheduledDropQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDropQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDropQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled drop quota steps in order and keeps a trace of each. */
data class ScheduledDropQuotaEngine(
    var steps: MutableList<ScheduledDropQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledDropQuota): Double {
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

    fun evaluateAll(items: MutableList<ScheduledDropQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledDropQuotaEngine(): ScheduledDropQuotaEngine {
    return ScheduledDropQuotaEngine(
        steps = mutableListOf<ScheduledDropQuotaFactor>(
            PercentScheduledDropQuotaAdjuster(7.1),
            RoundingScheduledDropQuotaAdjuster(0.1),
            MinimumScheduledDropQuotaAdjuster(17.7),
            CapScheduledDropQuotaAdjuster(2987.0),
            RegionScheduledDropQuotaAdjuster(
                mutableMapOf<String, Double>("north" to 1.04, "central" to 1.34, "south" to 0.71),
            ),
            TieredScheduledDropQuotaAdjuster(
                mutableListOf<Double>(333.0, 589.0, 1174.0),
                mutableListOf<Double>(1.19, 0.94, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 30226.7,
    )
}
