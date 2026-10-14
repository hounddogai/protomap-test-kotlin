package com.acme.transport.freightpay.dispute.recorditem

/** One step of the capped dispute claim calculation. */
interface CappedDisputeClaimFactor {
    fun describe(): String
    fun adjust(item: CappedDisputeClaim, value: Double): Double
}

data class MinimumCappedDisputeClaimAdjuster(var minimum: Double) : CappedDisputeClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDisputeClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedDisputeClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedDisputeClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedDisputeClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedDisputeClaimAdjuster(var cap: Double) : CappedDisputeClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedDisputeClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the capped dispute claim steps in order and keeps a trace of each. */
data class CappedDisputeClaimEngine(
    var steps: MutableList<CappedDisputeClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedDisputeClaim): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedDisputeClaimFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedDisputeClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedDisputeClaimEngine(): CappedDisputeClaimEngine {
    return CappedDisputeClaimEngine(
        steps = mutableListOf<CappedDisputeClaimFactor>(
            MinimumCappedDisputeClaimAdjuster(34.5),
            TieredCappedDisputeClaimAdjuster(
                mutableListOf<Double>(1784.0, 1913.0, 6940.0, 7228.0),
                mutableListOf<Double>(1.07, 1.1, 0.84, 0.86),
            ),
            CapCappedDisputeClaimAdjuster(26435.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 70670.8,
    )
}
