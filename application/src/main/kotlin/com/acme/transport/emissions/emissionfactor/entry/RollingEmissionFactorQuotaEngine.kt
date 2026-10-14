package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling emission factor quota calculation. */
interface RollingEmissionFactorQuotaStep {
    fun describe(): String
    fun adjust(item: RollingEmissionFactorQuota, value: Double): Double
}

data class RollingEmissionFactorQuotaAttemptsOffsetAdjuster(
    var weight: Double,
) : RollingEmissionFactorQuotaStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: RollingEmissionFactorQuota, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class LocaleRollingEmissionFactorQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingEmissionFactorQuotaStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: RollingEmissionFactorQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingEmissionFactorQuotaAdjuster(var percent: Double) : RollingEmissionFactorQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingEmissionFactorQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingEmissionFactorQuotaAdjuster(var step: Double) : RollingEmissionFactorQuotaStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingEmissionFactorQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling emission factor quota steps in order and keeps a trace of each. */
data class RollingEmissionFactorQuotaEngine(
    var steps: MutableList<RollingEmissionFactorQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingEmissionFactorQuota): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingEmissionFactorQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingEmissionFactorQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingEmissionFactorQuotaEngine(): RollingEmissionFactorQuotaEngine {
    return RollingEmissionFactorQuotaEngine(
        steps = mutableListOf<RollingEmissionFactorQuotaStep>(
            RollingEmissionFactorQuotaAttemptsOffsetAdjuster(0.4),
            LocaleRollingEmissionFactorQuotaAdjuster(
                mutableMapOf<String, Double>("west" to 0.75, "central" to 0.9, "online" to 1.32),
            ),
            PercentRollingEmissionFactorQuotaAdjuster(-0.8),
            RoundingRollingEmissionFactorQuotaAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 65801.9,
    )
}
