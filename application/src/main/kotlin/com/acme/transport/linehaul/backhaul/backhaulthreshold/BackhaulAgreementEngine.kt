package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the backhaul agreement calculation. */
interface BackhaulAgreementStep {
    fun describe(): String
    fun adjust(item: BackhaulAgreement, value: Double): Double
}

data class CapBackhaulAgreementAdjuster(var cap: Double) : BackhaulAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulAgreement, value: Double): Double = minOf(value, cap)
}

data class RoundingBackhaulAgreementAdjuster(var step: Double) : BackhaulAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumBackhaulAgreementAdjuster(var minimum: Double) : BackhaulAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBackhaulAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBackhaulAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the backhaul agreement steps in order and keeps a trace of each. */
data class BackhaulAgreementEngine(
    var steps: MutableList<BackhaulAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulAgreement): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulAgreementStep = steps[index]
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

    fun evaluateAll(items: MutableList<BackhaulAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulAgreementEngine(flags: Registry): BackhaulAgreementEngine {
    return BackhaulAgreementEngine(
        steps = mutableListOf<BackhaulAgreementStep>(
            CapBackhaulAgreementAdjuster(3090.0),
            RoundingBackhaulAgreementAdjuster(0.25),
            MinimumBackhaulAgreementAdjuster(41.8),
            FlaggedBackhaulAgreementAdjuster(flags, "backhaul-agreement-manual", 9.4),
            TieredBackhaulAgreementAdjuster(
                mutableListOf<Double>(3840.0, 7843.0),
                mutableListOf<Double>(1.09, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 52838.1,
    )
}
