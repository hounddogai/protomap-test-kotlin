package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the qualification quota calculation. */
interface QualificationQuotaAdjuster {
    fun describe(): String
    fun adjust(item: QualificationQuota, value: Double): Double
}

data class TieredQualificationQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapQualificationQuotaAdjuster(var cap: Double) : QualificationQuotaAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationQuota, value: Double): Double = minOf(value, cap)
}

data class PercentQualificationQuotaAdjuster(var percent: Double) : QualificationQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the qualification quota steps in order and keeps a trace of each. */
data class QualificationQuotaEngine(
    var steps: MutableList<QualificationQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: QualificationQuota): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultQualificationQuotaEngine(): QualificationQuotaEngine {
    return QualificationQuotaEngine(
        steps = mutableListOf<QualificationQuotaAdjuster>(
            TieredQualificationQuotaAdjuster(
                mutableListOf<Double>(2894.0, 3432.0, 6387.0),
                mutableListOf<Double>(1.06, 1.02, 0.95),
            ),
            CapQualificationQuotaAdjuster(12849.0),
            PercentQualificationQuotaAdjuster(2.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 97651.5,
    )
}
