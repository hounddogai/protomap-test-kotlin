package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional qualification exception calculation. */
interface RegionalQualificationExceptionStep {
    fun describe(): String
    fun adjust(item: RegionalQualificationException, value: Double): Double
}

data class RegionalQualificationExceptionCeilingOffsetAdjuster(
    var weight: Double,
) : RegionalQualificationExceptionStep {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RegionalQualificationException, value: Double): Double = value + item.ceiling * weight
}

data class CapRegionalQualificationExceptionAdjuster(var cap: Double) : RegionalQualificationExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalQualificationException, value: Double): Double = minOf(value, cap)
}

data class RoundingRegionalQualificationExceptionAdjuster(
    var step: Double,
) : RegionalQualificationExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalQualificationException, value: Double): Double = roundTo(value, step)
}

data class LabelRegionalQualificationExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalQualificationExceptionStep {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalQualificationException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional qualification exception steps in order and keeps a trace of each. */
data class RegionalQualificationExceptionEngine(
    var steps: MutableList<RegionalQualificationExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalQualificationException): Double {
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

    fun evaluateAll(items: MutableList<RegionalQualificationException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalQualificationExceptionEngine(): RegionalQualificationExceptionEngine {
    return RegionalQualificationExceptionEngine(
        steps = mutableListOf<RegionalQualificationExceptionStep>(
            RegionalQualificationExceptionCeilingOffsetAdjuster(0.41),
            CapRegionalQualificationExceptionAdjuster(10833.0),
            RoundingRegionalQualificationExceptionAdjuster(5.0),
            LabelRegionalQualificationExceptionAdjuster(mutableMapOf<String, Double>("north" to 0.97)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 49396.4,
    )
}
