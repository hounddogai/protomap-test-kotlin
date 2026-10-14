package com.acme.transport.network.hub.target

/** One step of the hub claim calculation. */
interface HubClaimModifier {
    fun describe(): String
    fun adjust(item: HubClaim, value: Double): Double
}

data class TieredHubClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class HubClaimLimitOffsetAdjuster(var weight: Double) : HubClaimModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: HubClaim, value: Double): Double = value + item.limit.toDouble() * weight
}

data class CapHubClaimAdjuster(var cap: Double) : HubClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the hub claim steps in order and keeps a trace of each. */
data class HubClaimEngine(
    var steps: MutableList<HubClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubClaim): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<HubClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubClaimEngine(): HubClaimEngine {
    return HubClaimEngine(
        steps = mutableListOf<HubClaimModifier>(
            TieredHubClaimAdjuster(
                mutableListOf<Double>(544.0, 2780.0, 3956.0, 6443.0),
                mutableListOf<Double>(1.08, 0.99, 1.13, 0.88),
            ),
            HubClaimLimitOffsetAdjuster(0.38),
            CapHubClaimAdjuster(35473.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 30549.6,
    )
}
