package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending hold quota calculation. */
interface PendingHoldQuotaFactor {
    fun describe(): String
    fun adjust(item: PendingHoldQuota, value: Double): Double
}

data class TieredPendingHoldQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingHoldQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingHoldQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LanePendingHoldQuotaAdjuster(var factors: MutableMap<String, Double>) : PendingHoldQuotaFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: PendingHoldQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingHoldQuotaAdjuster(var percent: Double) : PendingHoldQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingHoldQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPendingHoldQuotaAdjuster(var step: Double) : PendingHoldQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingHoldQuota, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingHoldQuotaAdjuster(var minimum: Double) : PendingHoldQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingHoldQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending hold quota steps in order and keeps a trace of each. */
data class PendingHoldQuotaEngine(
    var steps: MutableList<PendingHoldQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingHoldQuota): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingHoldQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingHoldQuotaEngine(): PendingHoldQuotaEngine {
    return PendingHoldQuotaEngine(
        steps = mutableListOf<PendingHoldQuotaFactor>(
            TieredPendingHoldQuotaAdjuster(
                mutableListOf<Double>(1313.0, 7606.0),
                mutableListOf<Double>(0.92, 0.91),
            ),
            LanePendingHoldQuotaAdjuster(
                mutableMapOf<String, Double>("central" to 0.86, "west" to 0.89, "retail" to 0.98),
            ),
            PercentPendingHoldQuotaAdjuster(-17.1),
            RoundingPendingHoldQuotaAdjuster(5.0),
            MinimumPendingHoldQuotaAdjuster(18.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 18788.1,
    )
}
