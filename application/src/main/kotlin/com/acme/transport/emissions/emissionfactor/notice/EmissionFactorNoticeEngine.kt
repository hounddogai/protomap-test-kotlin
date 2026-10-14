package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.kit.numbers.roundTo

/** One step of the emission factor notice calculation. */
interface EmissionFactorNoticeAdjuster {
    fun describe(): String
    fun adjust(item: EmissionFactorNotice, value: Double): Double
}

data class EmissionFactorNoticeUpdatedAtOffsetAdjuster(var weight: Double) : EmissionFactorNoticeAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: EmissionFactorNotice, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class LabelEmissionFactorNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorNoticeAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: EmissionFactorNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingEmissionFactorNoticeAdjuster(var step: Double) : EmissionFactorNoticeAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorNotice, value: Double): Double = roundTo(value, step)
}

data class MinimumEmissionFactorNoticeAdjuster(var minimum: Double) : EmissionFactorNoticeAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentEmissionFactorNoticeAdjuster(var percent: Double) : EmissionFactorNoticeAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapEmissionFactorNoticeAdjuster(var cap: Double) : EmissionFactorNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorNotice, value: Double): Double = minOf(value, cap)
}

/** Runs the emission factor notice steps in order and keeps a trace of each. */
data class EmissionFactorNoticeEngine(
    var steps: MutableList<EmissionFactorNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EmissionFactorNotice): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEmissionFactorNoticeEngine(): EmissionFactorNoticeEngine {
    return EmissionFactorNoticeEngine(
        steps = mutableListOf<EmissionFactorNoticeAdjuster>(
            EmissionFactorNoticeUpdatedAtOffsetAdjuster(0.28),
            LabelEmissionFactorNoticeAdjuster(mutableMapOf<String, Double>("south" to 1.08)),
            RoundingEmissionFactorNoticeAdjuster(0.05),
            MinimumEmissionFactorNoticeAdjuster(42.2),
            PercentEmissionFactorNoticeAdjuster(5.1),
            CapEmissionFactorNoticeAdjuster(37335.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 45625.6,
    )
}
