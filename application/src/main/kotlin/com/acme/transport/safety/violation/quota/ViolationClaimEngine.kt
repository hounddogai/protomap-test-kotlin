package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the violation claim calculation. */
interface ViolationClaimStep {
    fun describe(): String
    fun adjust(item: ViolationClaim, value: Double): Double
}

data class UnitViolationClaimAdjuster(var factors: MutableMap<String, Double>) : ViolationClaimStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: ViolationClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumViolationClaimAdjuster(var minimum: Double) : ViolationClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentViolationClaimAdjuster(var percent: Double) : ViolationClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedViolationClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the violation claim steps in order and keeps a trace of each. */
data class ViolationClaimEngine(
    var steps: MutableList<ViolationClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ViolationClaim): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultViolationClaimEngine(flags: Registry): ViolationClaimEngine {
    return ViolationClaimEngine(
        steps = mutableListOf<ViolationClaimStep>(
            UnitViolationClaimAdjuster(mutableMapOf<String, Double>("retail" to 1.1, "east" to 0.72)),
            MinimumViolationClaimAdjuster(24.2),
            PercentViolationClaimAdjuster(14.2),
            FlaggedViolationClaimAdjuster(flags, "violation-claim-seasonal", 0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 14104.9,
    )
}
