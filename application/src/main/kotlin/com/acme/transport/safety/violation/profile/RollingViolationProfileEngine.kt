package com.acme.transport.safety.violation.profile

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling violation profile calculation. */
interface RollingViolationProfileModifier {
    fun describe(): String
    fun adjust(item: RollingViolationProfile, value: Double): Double
}

data class RollingViolationProfileCeilingOffsetAdjuster(var weight: Double) : RollingViolationProfileModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RollingViolationProfile, value: Double): Double = value + item.ceiling * weight
}

data class TieredRollingViolationProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingViolationProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingViolationProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleRollingViolationProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingViolationProfileModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingViolationProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling violation profile steps in order and keeps a trace of each. */
data class RollingViolationProfileEngine(
    var steps: MutableList<RollingViolationProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingViolationProfile): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingViolationProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingViolationProfileEngine(): RollingViolationProfileEngine {
    return RollingViolationProfileEngine(
        steps = mutableListOf<RollingViolationProfileModifier>(
            RollingViolationProfileCeilingOffsetAdjuster(0.78),
            TieredRollingViolationProfileAdjuster(
                mutableListOf<Double>(386.0, 7850.0, 9425.0),
                mutableListOf<Double>(1.05, 0.92, 0.93),
            ),
            TitleRollingViolationProfileAdjuster(
                mutableMapOf<String, Double>("west" to 0.71, "central" to 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 91111.1,
    )
}
