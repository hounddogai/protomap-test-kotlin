package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.flags.Registry

/** One step of the proof of delivery ledger calculation. */
interface ProofOfDeliveryLedgerStep {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryLedger, value: Double): Double
}

data class TieredProofOfDeliveryLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedProofOfDeliveryLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelProofOfDeliveryLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryLedgerStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: ProofOfDeliveryLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the proof of delivery ledger steps in order and keeps a trace of each. */
data class ProofOfDeliveryLedgerEngine(
    var steps: MutableList<ProofOfDeliveryLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ProofOfDeliveryLedger): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryLedgerEngine(flags: Registry): ProofOfDeliveryLedgerEngine {
    return ProofOfDeliveryLedgerEngine(
        steps = mutableListOf<ProofOfDeliveryLedgerStep>(
            TieredProofOfDeliveryLedgerAdjuster(
                mutableListOf<Double>(2135.0, 3130.0, 3659.0),
                mutableListOf<Double>(1.01, 0.94, 1.03),
            ),
            FlaggedProofOfDeliveryLedgerAdjuster(flags, "proof-of-delivery-ledger-primary", -1.8),
            ChannelProofOfDeliveryLedgerAdjuster(
                mutableMapOf<String, Double>("retail" to 1.34, "north" to 1.33, "central" to 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 92572.7,
    )
}
