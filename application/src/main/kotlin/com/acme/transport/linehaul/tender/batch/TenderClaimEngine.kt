package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.flags.Registry

/** One step of the tender claim calculation. */
interface TenderClaimAdjuster {
    fun describe(): String
    fun adjust(item: TenderClaim, value: Double): Double
}

data class LaneTenderClaimAdjuster(var factors: MutableMap<String, Double>) : TenderClaimAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: TenderClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTenderClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTenderClaimAdjuster(var percent: Double) : TenderClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tender claim steps in order and keeps a trace of each. */
data class TenderClaimEngine(
    var steps: MutableList<TenderClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TenderClaim): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TenderClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTenderClaimEngine(flags: Registry): TenderClaimEngine {
    return TenderClaimEngine(
        steps = mutableListOf<TenderClaimAdjuster>(
            LaneTenderClaimAdjuster(
                mutableMapOf<String, Double>("retail" to 0.96, "central" to 1.2, "online" to 0.92),
            ),
            FlaggedTenderClaimAdjuster(flags, "tender-claim-scheduled", 8.6),
            PercentTenderClaimAdjuster(23.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 90696.4,
    )
}
