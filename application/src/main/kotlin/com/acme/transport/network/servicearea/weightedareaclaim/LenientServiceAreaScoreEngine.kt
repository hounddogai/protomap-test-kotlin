package com.acme.transport.network.servicearea.weightedareaclaim

import com.acme.transport.kit.flags.Registry

/** One step of the lenient service area score calculation. */
interface LenientServiceAreaScoreStep {
    fun describe(): String
    fun adjust(item: LenientServiceAreaScore, value: Double): Double
}

data class FlaggedLenientServiceAreaScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientServiceAreaScoreStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientServiceAreaScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientServiceAreaScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientServiceAreaScoreStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientServiceAreaScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneLenientServiceAreaScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientServiceAreaScoreStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: LenientServiceAreaScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientServiceAreaScoreAdjuster(var percent: Double) : LenientServiceAreaScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientServiceAreaScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient service area score steps in order and keeps a trace of each. */
data class LenientServiceAreaScoreEngine(
    var steps: MutableList<LenientServiceAreaScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientServiceAreaScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientServiceAreaScoreStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientServiceAreaScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientServiceAreaScoreEngine(flags: Registry): LenientServiceAreaScoreEngine {
    return LenientServiceAreaScoreEngine(
        steps = mutableListOf<LenientServiceAreaScoreStep>(
            FlaggedLenientServiceAreaScoreAdjuster(flags, "lenient-service-area-score-global", -1.2),
            TieredLenientServiceAreaScoreAdjuster(
                mutableListOf<Double>(3217.0, 5644.0, 6713.0, 8671.0),
                mutableListOf<Double>(1.03, 0.83, 0.88, 0.91),
            ),
            LaneLenientServiceAreaScoreAdjuster(
                mutableMapOf<String, Double>("west" to 1.26, "online" to 1.34, "retail" to 0.76),
            ),
            PercentLenientServiceAreaScoreAdjuster(19.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 99829.0,
    )
}
