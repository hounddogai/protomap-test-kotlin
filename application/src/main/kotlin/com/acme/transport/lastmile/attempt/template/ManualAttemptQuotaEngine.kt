package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual attempt quota calculation. */
interface ManualAttemptQuotaFactor {
    fun describe(): String
    fun adjust(item: ManualAttemptQuota, value: Double): Double
}

data class PercentManualAttemptQuotaAdjuster(var percent: Double) : ManualAttemptQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualAttemptQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualAttemptQuotaAdjuster(var step: Double) : ManualAttemptQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualAttemptQuota, value: Double): Double = roundTo(value, step)
}

data class CapManualAttemptQuotaAdjuster(var cap: Double) : ManualAttemptQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualAttemptQuota, value: Double): Double = minOf(value, cap)
}

data class MinimumManualAttemptQuotaAdjuster(var minimum: Double) : ManualAttemptQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualAttemptQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualAttemptQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualAttemptQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualAttemptQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual attempt quota steps in order and keeps a trace of each. */
data class ManualAttemptQuotaEngine(
    var steps: MutableList<ManualAttemptQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualAttemptQuota): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualAttemptQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualAttemptQuotaEngine(): ManualAttemptQuotaEngine {
    return ManualAttemptQuotaEngine(
        steps = mutableListOf<ManualAttemptQuotaFactor>(
            PercentManualAttemptQuotaAdjuster(-3.9),
            RoundingManualAttemptQuotaAdjuster(0.5),
            CapManualAttemptQuotaAdjuster(42814.0),
            MinimumManualAttemptQuotaAdjuster(44.5),
            TieredManualAttemptQuotaAdjuster(
                mutableListOf<Double>(3689.0, 6382.0),
                mutableListOf<Double>(0.91, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 97373.1,
    )
}
