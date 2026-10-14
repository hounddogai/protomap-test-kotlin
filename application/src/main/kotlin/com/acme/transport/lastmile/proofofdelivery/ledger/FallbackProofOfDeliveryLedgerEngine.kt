package com.acme.transport.lastmile.proofofdelivery.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback proof of delivery ledger calculation. */
interface FallbackProofOfDeliveryLedgerFactor {
    fun describe(): String
    fun adjust(item: FallbackProofOfDeliveryLedger, value: Double): Double
}

data class FallbackProofOfDeliveryLedgerFuelLitersOffsetAdjuster(
    var weight: Double,
) : FallbackProofOfDeliveryLedgerFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: FallbackProofOfDeliveryLedger, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedFallbackProofOfDeliveryLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackProofOfDeliveryLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackProofOfDeliveryLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFallbackProofOfDeliveryLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackProofOfDeliveryLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackProofOfDeliveryLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback proof of delivery ledger steps in order and keeps a trace of each. */
data class FallbackProofOfDeliveryLedgerEngine(
    var steps: MutableList<FallbackProofOfDeliveryLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackProofOfDeliveryLedger): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackProofOfDeliveryLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackProofOfDeliveryLedgerEngine(flags: Registry): FallbackProofOfDeliveryLedgerEngine {
    return FallbackProofOfDeliveryLedgerEngine(
        steps = mutableListOf<FallbackProofOfDeliveryLedgerFactor>(
            FallbackProofOfDeliveryLedgerFuelLitersOffsetAdjuster(1.13),
            FlaggedFallbackProofOfDeliveryLedgerAdjuster(
                flags,
                "fallback-proof-of-delivery-ledger-strict",
                -8.8,
            ),
            TieredFallbackProofOfDeliveryLedgerAdjuster(
                mutableListOf<Double>(2295.0, 2626.0, 4878.0),
                mutableListOf<Double>(0.84, 1.18, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 92424.2,
    )
}
