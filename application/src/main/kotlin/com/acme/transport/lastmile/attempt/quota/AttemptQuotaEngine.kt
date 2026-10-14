package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the attempt quota calculation. */
interface AttemptQuotaModifier {
    fun describe(): String
    fun adjust(item: AttemptQuota, value: Double): Double
}

data class UnitAttemptQuotaAdjuster(var factors: MutableMap<String, Double>) : AttemptQuotaModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: AttemptQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AttemptQuotaOdometerOffsetAdjuster(var weight: Double) : AttemptQuotaModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: AttemptQuota, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class FlaggedAttemptQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAttemptQuotaAdjuster(var cap: Double) : AttemptQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptQuota, value: Double): Double = minOf(value, cap)
}

data class RoundingAttemptQuotaAdjuster(var step: Double) : AttemptQuotaModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptQuota, value: Double): Double = roundTo(value, step)
}

data class TieredAttemptQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the attempt quota steps in order and keeps a trace of each. */
data class AttemptQuotaEngine(
    var steps: MutableList<AttemptQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptQuota): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptQuotaEngine(flags: Registry): AttemptQuotaEngine {
    return AttemptQuotaEngine(
        steps = mutableListOf<AttemptQuotaModifier>(
            UnitAttemptQuotaAdjuster(mutableMapOf<String, Double>("west" to 1.36)),
            AttemptQuotaOdometerOffsetAdjuster(1.56),
            FlaggedAttemptQuotaAdjuster(flags, "attempt-quota-rolling", -0.4),
            CapAttemptQuotaAdjuster(38055.0),
            RoundingAttemptQuotaAdjuster(0.25),
            TieredAttemptQuotaAdjuster(
                mutableListOf<Double>(6737.0, 7026.0, 8273.0),
                mutableListOf<Double>(1.16, 0.86, 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 96880.3,
    )
}
