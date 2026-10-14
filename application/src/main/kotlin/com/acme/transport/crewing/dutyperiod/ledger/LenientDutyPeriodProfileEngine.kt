package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient duty period profile calculation. */
interface LenientDutyPeriodProfileFactor {
    fun describe(): String
    fun adjust(item: LenientDutyPeriodProfile, value: Double): Double
}

data class TieredLenientDutyPeriodProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDutyPeriodProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDutyPeriodProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameLenientDutyPeriodProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDutyPeriodProfileFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: LenientDutyPeriodProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientDutyPeriodProfileAdjuster(var percent: Double) : LenientDutyPeriodProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDutyPeriodProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient duty period profile steps in order and keeps a trace of each. */
data class LenientDutyPeriodProfileEngine(
    var steps: MutableList<LenientDutyPeriodProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientDutyPeriodProfile): Double {
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

    fun evaluateAll(items: MutableList<LenientDutyPeriodProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientDutyPeriodProfileEngine(): LenientDutyPeriodProfileEngine {
    return LenientDutyPeriodProfileEngine(
        steps = mutableListOf<LenientDutyPeriodProfileFactor>(
            TieredLenientDutyPeriodProfileAdjuster(
                mutableListOf<Double>(5891.0, 9521.0),
                mutableListOf<Double>(1.03, 0.95),
            ),
            NameLenientDutyPeriodProfileAdjuster(
                mutableMapOf<String, Double>("north" to 0.82, "west" to 0.71, "online" to 0.9),
            ),
            PercentLenientDutyPeriodProfileAdjuster(-4.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 14904.0,
    )
}
