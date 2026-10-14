package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the drop agreement calculation. */
interface DropAgreementFactor {
    fun describe(): String
    fun adjust(item: DropAgreement, value: Double): Double
}

data class TieredDropAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDropAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDropAgreementAdjuster(var percent: Double) : DropAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDropAgreementAdjuster(var step: Double) : DropAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropAgreement, value: Double): Double = roundTo(value, step)
}

data class CategoryDropAgreementAdjuster(var factors: MutableMap<String, Double>) : DropAgreementFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: DropAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the drop agreement steps in order and keeps a trace of each. */
data class DropAgreementEngine(
    var steps: MutableList<DropAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DropAgreement): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropAgreementFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DropAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDropAgreementEngine(flags: Registry): DropAgreementEngine {
    return DropAgreementEngine(
        steps = mutableListOf<DropAgreementFactor>(
            TieredDropAgreementAdjuster(
                mutableListOf<Double>(2533.0, 6263.0),
                mutableListOf<Double>(0.96, 0.82),
            ),
            FlaggedDropAgreementAdjuster(flags, "drop-agreement-weighted", 6.3),
            PercentDropAgreementAdjuster(0.2),
            RoundingDropAgreementAdjuster(0.1),
            CategoryDropAgreementAdjuster(
                mutableMapOf<String, Double>("online" to 0.84, "retail" to 0.99, "north" to 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 48454.4,
    )
}
