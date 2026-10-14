package com.acme.transport.fleetops.serviceplan.quota

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient service plan quota calculation. */
interface LenientServicePlanQuotaModifier {
    fun describe(): String
    fun adjust(item: LenientServicePlanQuota, value: Double): Double
}

data class PercentLenientServicePlanQuotaAdjuster(var percent: Double) : LenientServicePlanQuotaModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientServicePlanQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelLenientServicePlanQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientServicePlanQuotaModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientServicePlanQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLenientServicePlanQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientServicePlanQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientServicePlanQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LenientServicePlanQuotaBatchSizeOffsetAdjuster(
    var weight: Double,
) : LenientServicePlanQuotaModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: LenientServicePlanQuota, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class CapLenientServicePlanQuotaAdjuster(var cap: Double) : LenientServicePlanQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientServicePlanQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient service plan quota steps in order and keeps a trace of each. */
data class LenientServicePlanQuotaEngine(
    var steps: MutableList<LenientServicePlanQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientServicePlanQuota): Double {
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

    fun evaluateAll(items: MutableList<LenientServicePlanQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientServicePlanQuotaEngine(): LenientServicePlanQuotaEngine {
    return LenientServicePlanQuotaEngine(
        steps = mutableListOf<LenientServicePlanQuotaModifier>(
            PercentLenientServicePlanQuotaAdjuster(-16.3),
            LabelLenientServicePlanQuotaAdjuster(
                mutableMapOf<String, Double>("online" to 1.17, "north" to 1.37, "east" to 1.1),
            ),
            TieredLenientServicePlanQuotaAdjuster(
                mutableListOf<Double>(1142.0, 2314.0, 4881.0),
                mutableListOf<Double>(1.11, 0.86, 0.87),
            ),
            LenientServicePlanQuotaBatchSizeOffsetAdjuster(0.1),
            CapLenientServicePlanQuotaAdjuster(46187.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 41624.1,
    )
}
