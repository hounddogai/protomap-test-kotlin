package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted trailer yard agreement calculation. */
interface WeightedTrailerYardAgreementFactor {
    fun describe(): String
    fun adjust(item: WeightedTrailerYardAgreement, value: Double): Double
}

data class FlaggedWeightedTrailerYardAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedTrailerYardAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedTrailerYardAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumWeightedTrailerYardAgreementAdjuster(
    var minimum: Double,
) : WeightedTrailerYardAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedTrailerYardAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapWeightedTrailerYardAgreementAdjuster(var cap: Double) : WeightedTrailerYardAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedTrailerYardAgreement, value: Double): Double = minOf(value, cap)
}

data class PercentWeightedTrailerYardAgreementAdjuster(
    var percent: Double,
) : WeightedTrailerYardAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedTrailerYardAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted trailer yard agreement steps in order and keeps a trace of each. */
data class WeightedTrailerYardAgreementEngine(
    var steps: MutableList<WeightedTrailerYardAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedTrailerYardAgreement): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedTrailerYardAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedTrailerYardAgreementEngine(flags: Registry): WeightedTrailerYardAgreementEngine {
    return WeightedTrailerYardAgreementEngine(
        steps = mutableListOf<WeightedTrailerYardAgreementFactor>(
            FlaggedWeightedTrailerYardAgreementAdjuster(flags, "weighted-trailer-yard-agreement-weighted", 3.7),
            MinimumWeightedTrailerYardAgreementAdjuster(15.9),
            CapWeightedTrailerYardAgreementAdjuster(41244.0),
            PercentWeightedTrailerYardAgreementAdjuster(0.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 87455.2,
    )
}
