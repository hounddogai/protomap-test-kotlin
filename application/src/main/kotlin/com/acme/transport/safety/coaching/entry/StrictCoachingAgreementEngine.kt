package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict coaching agreement calculation. */
interface StrictCoachingAgreementFactor {
    fun describe(): String
    fun adjust(item: StrictCoachingAgreement, value: Double): Double
}

data class StrictCoachingAgreementDistanceKmOffsetAdjuster(var weight: Double) : StrictCoachingAgreementFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: StrictCoachingAgreement, value: Double): Double = value + item.distanceKm * weight
}

data class PercentStrictCoachingAgreementAdjuster(var percent: Double) : StrictCoachingAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictCoachingAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedStrictCoachingAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictCoachingAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictCoachingAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict coaching agreement steps in order and keeps a trace of each. */
data class StrictCoachingAgreementEngine(
    var steps: MutableList<StrictCoachingAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictCoachingAgreement): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictCoachingAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictCoachingAgreementEngine(flags: Registry): StrictCoachingAgreementEngine {
    return StrictCoachingAgreementEngine(
        steps = mutableListOf<StrictCoachingAgreementFactor>(
            StrictCoachingAgreementDistanceKmOffsetAdjuster(1.87),
            PercentStrictCoachingAgreementAdjuster(-22.8),
            FlaggedStrictCoachingAgreementAdjuster(flags, "strict-coaching-agreement-tiered", -0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 99103.5,
    )
}
