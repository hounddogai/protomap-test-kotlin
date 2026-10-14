package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional qualification claim calculation. */
interface RegionalQualificationClaimStep {
    fun describe(): String
    fun adjust(item: RegionalQualificationClaim, value: Double): Double
}

data class NameRegionalQualificationClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalQualificationClaimStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalQualificationClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRegionalQualificationClaimAdjuster(var step: Double) : RegionalQualificationClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalQualificationClaim, value: Double): Double = roundTo(value, step)
}

data class FlaggedRegionalQualificationClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalQualificationClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalQualificationClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional qualification claim steps in order and keeps a trace of each. */
data class RegionalQualificationClaimEngine(
    var steps: MutableList<RegionalQualificationClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalQualificationClaim): Double {
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

    fun evaluateAll(items: MutableList<RegionalQualificationClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalQualificationClaimEngine(flags: Registry): RegionalQualificationClaimEngine {
    return RegionalQualificationClaimEngine(
        steps = mutableListOf<RegionalQualificationClaimStep>(
            NameRegionalQualificationClaimAdjuster(
                mutableMapOf<String, Double>("central" to 1.25, "south" to 1.16),
            ),
            RoundingRegionalQualificationClaimAdjuster(0.05),
            FlaggedRegionalQualificationClaimAdjuster(flags, "regional-qualification-claim-lenient", 0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 23537.9,
    )
}
