package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default proof of delivery ledger calculation. */
interface DefaultProofOfDeliveryLedgerStep {
    fun describe(): String
    fun adjust(item: DefaultProofOfDeliveryLedger, value: Double): Double
}

data class PercentDefaultProofOfDeliveryLedgerAdjuster(var percent: Double) : DefaultProofOfDeliveryLedgerStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultProofOfDeliveryLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DefaultProofOfDeliveryLedgerLimitOffsetAdjuster(
    var weight: Double,
) : DefaultProofOfDeliveryLedgerStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: DefaultProofOfDeliveryLedger, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class CapDefaultProofOfDeliveryLedgerAdjuster(var cap: Double) : DefaultProofOfDeliveryLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultProofOfDeliveryLedger, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultProofOfDeliveryLedgerAdjuster(var minimum: Double) : DefaultProofOfDeliveryLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultProofOfDeliveryLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleDefaultProofOfDeliveryLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultProofOfDeliveryLedgerStep {
    override fun describe(): String = "by title"

    override fun adjust(item: DefaultProofOfDeliveryLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default proof of delivery ledger steps in order and keeps a trace of each. */
data class DefaultProofOfDeliveryLedgerEngine(
    var steps: MutableList<DefaultProofOfDeliveryLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultProofOfDeliveryLedger): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultProofOfDeliveryLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultProofOfDeliveryLedgerEngine(): DefaultProofOfDeliveryLedgerEngine {
    return DefaultProofOfDeliveryLedgerEngine(
        steps = mutableListOf<DefaultProofOfDeliveryLedgerStep>(
            PercentDefaultProofOfDeliveryLedgerAdjuster(18.2),
            DefaultProofOfDeliveryLedgerLimitOffsetAdjuster(1.17),
            CapDefaultProofOfDeliveryLedgerAdjuster(34394.0),
            MinimumDefaultProofOfDeliveryLedgerAdjuster(13.1),
            TitleDefaultProofOfDeliveryLedgerAdjuster(
                mutableMapOf<String, Double>("west" to 1.39, "central" to 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 3015.9,
    )
}
