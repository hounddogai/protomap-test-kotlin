package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer agreement calculation. */
interface TrailerAgreementAdjuster {
    fun describe(): String
    fun adjust(item: TrailerAgreement, value: Double): Double
}

data class CategoryTrailerAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : TrailerAgreementAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: TrailerAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TrailerAgreementLoadKgOffsetAdjuster(var weight: Double) : TrailerAgreementAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: TrailerAgreement, value: Double): Double = value + item.loadKg * weight
}

data class PercentTrailerAgreementAdjuster(var percent: Double) : TrailerAgreementAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTrailerAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTrailerAgreementAdjuster(var step: Double) : TrailerAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumTrailerAgreementAdjuster(var minimum: Double) : TrailerAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the trailer agreement steps in order and keeps a trace of each. */
data class TrailerAgreementEngine(
    var steps: MutableList<TrailerAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerAgreement): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerAgreementEngine(flags: Registry): TrailerAgreementEngine {
    return TrailerAgreementEngine(
        steps = mutableListOf<TrailerAgreementAdjuster>(
            CategoryTrailerAgreementAdjuster(mutableMapOf<String, Double>("online" to 0.75)),
            TrailerAgreementLoadKgOffsetAdjuster(1.8),
            PercentTrailerAgreementAdjuster(-15.6),
            FlaggedTrailerAgreementAdjuster(flags, "trailer-agreement-lenient", 7.4),
            RoundingTrailerAgreementAdjuster(0.5),
            MinimumTrailerAgreementAdjuster(8.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 17749.3,
    )
}
