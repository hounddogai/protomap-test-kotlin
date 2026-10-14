package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped tender claim calculation. */
interface CappedTenderClaimAdjuster {
    fun describe(): String
    fun adjust(item: CappedTenderClaim, value: Double): Double
}

data class NameCappedTenderClaimAdjuster(var factors: MutableMap<String, Double>) : CappedTenderClaimAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedTenderClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedTenderClaimAdjuster(var step: Double) : CappedTenderClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedTenderClaim, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedTenderClaimAdjuster(var minimum: Double) : CappedTenderClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedTenderClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedTenderClaimAdjuster(var percent: Double) : CappedTenderClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedTenderClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedTenderClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTenderClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTenderClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedTenderClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedTenderClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedTenderClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped tender claim steps in order and keeps a trace of each. */
data class CappedTenderClaimEngine(
    var steps: MutableList<CappedTenderClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedTenderClaim): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedTenderClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedTenderClaimEngine(flags: Registry): CappedTenderClaimEngine {
    return CappedTenderClaimEngine(
        steps = mutableListOf<CappedTenderClaimAdjuster>(
            NameCappedTenderClaimAdjuster(mutableMapOf<String, Double>("central" to 1.02, "east" to 0.85)),
            RoundingCappedTenderClaimAdjuster(0.05),
            MinimumCappedTenderClaimAdjuster(34.4),
            PercentCappedTenderClaimAdjuster(-24.5),
            FlaggedCappedTenderClaimAdjuster(flags, "capped-tender-claim-weighted", -0.7),
            TieredCappedTenderClaimAdjuster(
                mutableListOf<Double>(3430.0, 6111.0, 8996.0, 9533.0),
                mutableListOf<Double>(0.82, 0.9, 1.18, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 95370.4,
    )
}
