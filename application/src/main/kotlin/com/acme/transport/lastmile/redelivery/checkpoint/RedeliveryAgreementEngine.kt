package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the redelivery agreement calculation. */
interface RedeliveryAgreementModifier {
    fun describe(): String
    fun adjust(item: RedeliveryAgreement, value: Double): Double
}

data class MinimumRedeliveryAgreementAdjuster(var minimum: Double) : RedeliveryAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRedeliveryAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRedeliveryAgreementAdjuster(var step: Double) : RedeliveryAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryAgreement, value: Double): Double = roundTo(value, step)
}

data class PercentRedeliveryAgreementAdjuster(var percent: Double) : RedeliveryAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRedeliveryAgreementAdjuster(var cap: Double) : RedeliveryAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryAgreement, value: Double): Double = minOf(value, cap)
}

data class RedeliveryAgreementCreatedAtOffsetAdjuster(var weight: Double) : RedeliveryAgreementModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: RedeliveryAgreement, value: Double): Double = value + item.createdAt.toDouble() * weight
}

/** Runs the redelivery agreement steps in order and keeps a trace of each. */
data class RedeliveryAgreementEngine(
    var steps: MutableList<RedeliveryAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RedeliveryAgreement): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RedeliveryAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRedeliveryAgreementEngine(): RedeliveryAgreementEngine {
    return RedeliveryAgreementEngine(
        steps = mutableListOf<RedeliveryAgreementModifier>(
            MinimumRedeliveryAgreementAdjuster(46.7),
            TieredRedeliveryAgreementAdjuster(
                mutableListOf<Double>(161.0, 565.0),
                mutableListOf<Double>(0.86, 1.05),
            ),
            RoundingRedeliveryAgreementAdjuster(0.1),
            PercentRedeliveryAgreementAdjuster(-10.1),
            CapRedeliveryAgreementAdjuster(5053.0),
            RedeliveryAgreementCreatedAtOffsetAdjuster(1.33),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 67100.4,
    )
}
