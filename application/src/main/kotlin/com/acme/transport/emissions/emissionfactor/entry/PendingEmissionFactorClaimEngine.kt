package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending emission factor claim calculation. */
interface PendingEmissionFactorClaimStep {
    fun describe(): String
    fun adjust(item: PendingEmissionFactorClaim, value: Double): Double
}

data class CapPendingEmissionFactorClaimAdjuster(var cap: Double) : PendingEmissionFactorClaimStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingEmissionFactorClaim, value: Double): Double = minOf(value, cap)
}

data class FlaggedPendingEmissionFactorClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingEmissionFactorClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingEmissionFactorClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingEmissionFactorClaimAdjuster(var minimum: Double) : PendingEmissionFactorClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingEmissionFactorClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPendingEmissionFactorClaimAdjuster(var step: Double) : PendingEmissionFactorClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingEmissionFactorClaim, value: Double): Double = roundTo(value, step)
}

/** Runs the pending emission factor claim steps in order and keeps a trace of each. */
data class PendingEmissionFactorClaimEngine(
    var steps: MutableList<PendingEmissionFactorClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingEmissionFactorClaim): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingEmissionFactorClaimStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingEmissionFactorClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingEmissionFactorClaimEngine(flags: Registry): PendingEmissionFactorClaimEngine {
    return PendingEmissionFactorClaimEngine(
        steps = mutableListOf<PendingEmissionFactorClaimStep>(
            CapPendingEmissionFactorClaimAdjuster(37690.0),
            FlaggedPendingEmissionFactorClaimAdjuster(flags, "pending-emission-factor-claim-lenient", -1.3),
            MinimumPendingEmissionFactorClaimAdjuster(16.3),
            RoundingPendingEmissionFactorClaimAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 49475.2,
    )
}
