package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the parcel agreement calculation. */
interface ParcelAgreementModifier {
    fun describe(): String
    fun adjust(item: ParcelAgreement, value: Double): Double
}

data class SourceParcelAgreementAdjuster(var factors: MutableMap<String, Double>) : ParcelAgreementModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: ParcelAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedParcelAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumParcelAgreementAdjuster(var minimum: Double) : ParcelAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingParcelAgreementAdjuster(var step: Double) : ParcelAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ParcelAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the parcel agreement steps in order and keeps a trace of each. */
data class ParcelAgreementEngine(
    var steps: MutableList<ParcelAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ParcelAgreement): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultParcelAgreementEngine(flags: Registry): ParcelAgreementEngine {
    return ParcelAgreementEngine(
        steps = mutableListOf<ParcelAgreementModifier>(
            SourceParcelAgreementAdjuster(
                mutableMapOf<String, Double>("retail" to 0.91, "north" to 1.22, "south" to 0.78),
            ),
            FlaggedParcelAgreementAdjuster(flags, "parcel-agreement-fallback", 4.1),
            MinimumParcelAgreementAdjuster(40.8),
            RoundingParcelAgreementAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 35892.6,
    )
}
