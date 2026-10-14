package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.flags.Registry

/** One step of the fuel burn claim calculation. */
interface FuelBurnClaimAdjuster {
    fun describe(): String
    fun adjust(item: FuelBurnClaim, value: Double): Double
}

data class FlaggedFuelBurnClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFuelBurnClaimAdjuster(var minimum: Double) : FuelBurnClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFuelBurnClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFuelBurnClaimAdjuster(var cap: Double) : FuelBurnClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel burn claim steps in order and keeps a trace of each. */
data class FuelBurnClaimEngine(
    var steps: MutableList<FuelBurnClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnClaim): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnClaimEngine(flags: Registry): FuelBurnClaimEngine {
    return FuelBurnClaimEngine(
        steps = mutableListOf<FuelBurnClaimAdjuster>(
            FlaggedFuelBurnClaimAdjuster(flags, "fuel-burn-claim-fallback", -9.3),
            MinimumFuelBurnClaimAdjuster(45.0),
            TieredFuelBurnClaimAdjuster(mutableListOf<Double>(3281.0, 9039.0), mutableListOf<Double>(1.11, 0.8)),
            CapFuelBurnClaimAdjuster(2134.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 68607.1,
    )
}
