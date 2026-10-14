package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the duty period quota calculation. */
interface DutyPeriodQuotaModifier {
    fun describe(): String
    fun adjust(item: DutyPeriodQuota, value: Double): Double
}

data class DutyPeriodQuotaMarginOffsetAdjuster(var weight: Double) : DutyPeriodQuotaModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: DutyPeriodQuota, value: Double): Double = value + item.margin * weight
}

data class TieredDutyPeriodQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDutyPeriodQuotaAdjuster(var step: Double) : DutyPeriodQuotaModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodQuota, value: Double): Double = roundTo(value, step)
}

data class CapDutyPeriodQuotaAdjuster(var cap: Double) : DutyPeriodQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DutyPeriodQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the duty period quota steps in order and keeps a trace of each. */
data class DutyPeriodQuotaEngine(
    var steps: MutableList<DutyPeriodQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DutyPeriodQuota): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDutyPeriodQuotaEngine(): DutyPeriodQuotaEngine {
    return DutyPeriodQuotaEngine(
        steps = mutableListOf<DutyPeriodQuotaModifier>(
            DutyPeriodQuotaMarginOffsetAdjuster(0.49),
            TieredDutyPeriodQuotaAdjuster(
                mutableListOf<Double>(651.0, 1998.0, 4352.0, 5918.0),
                mutableListOf<Double>(0.86, 1.08, 1.17, 1.07),
            ),
            RoundingDutyPeriodQuotaAdjuster(0.1),
            CapDutyPeriodQuotaAdjuster(15393.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 25564.7,
    )
}
