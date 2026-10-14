package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending tariff claim calculation. */
interface PendingTariffClaimModifier {
    fun describe(): String
    fun adjust(item: PendingTariffClaim, value: Double): Double
}

data class RoundingPendingTariffClaimAdjuster(var step: Double) : PendingTariffClaimModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTariffClaim, value: Double): Double = roundTo(value, step)
}

data class FlaggedPendingTariffClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingTariffClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingTariffClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingTariffClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingTariffClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingTariffClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PendingTariffClaimPalletsOffsetAdjuster(var weight: Double) : PendingTariffClaimModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: PendingTariffClaim, value: Double): Double = value + item.pallets.toDouble() * weight
}

/** Runs the pending tariff claim steps in order and keeps a trace of each. */
data class PendingTariffClaimEngine(
    var steps: MutableList<PendingTariffClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingTariffClaim): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingTariffClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingTariffClaimEngine(flags: Registry): PendingTariffClaimEngine {
    return PendingTariffClaimEngine(
        steps = mutableListOf<PendingTariffClaimModifier>(
            RoundingPendingTariffClaimAdjuster(0.1),
            FlaggedPendingTariffClaimAdjuster(flags, "pending-tariff-claim-rolling", -2.7),
            TieredPendingTariffClaimAdjuster(
                mutableListOf<Double>(7787.0, 8737.0),
                mutableListOf<Double>(0.88, 1.02),
            ),
            PendingTariffClaimPalletsOffsetAdjuster(1.46),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 33097.9,
    )
}
