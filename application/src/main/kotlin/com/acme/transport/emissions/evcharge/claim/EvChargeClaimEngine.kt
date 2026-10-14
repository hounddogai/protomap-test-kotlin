package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the ev charge claim calculation. */
interface EvChargeClaimFactor {
    fun describe(): String
    fun adjust(item: EvChargeClaim, value: Double): Double
}

data class CapEvChargeClaimAdjuster(var cap: Double) : EvChargeClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeClaim, value: Double): Double = minOf(value, cap)
}

data class MinimumEvChargeClaimAdjuster(var minimum: Double) : EvChargeClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingEvChargeClaimAdjuster(var step: Double) : EvChargeClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeClaim, value: Double): Double = roundTo(value, step)
}

data class FlaggedEvChargeClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryEvChargeClaimAdjuster(var factors: MutableMap<String, Double>) : EvChargeClaimFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: EvChargeClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the ev charge claim steps in order and keeps a trace of each. */
data class EvChargeClaimEngine(
    var steps: MutableList<EvChargeClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EvChargeClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeClaimFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEvChargeClaimEngine(flags: Registry): EvChargeClaimEngine {
    return EvChargeClaimEngine(
        steps = mutableListOf<EvChargeClaimFactor>(
            CapEvChargeClaimAdjuster(37680.0),
            MinimumEvChargeClaimAdjuster(19.9),
            RoundingEvChargeClaimAdjuster(0.1),
            FlaggedEvChargeClaimAdjuster(flags, "ev-charge-claim-global", 6.8),
            CountryEvChargeClaimAdjuster(
                mutableMapOf<String, Double>("south" to 1.1, "east" to 0.76, "north" to 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 45357.0,
    )
}
