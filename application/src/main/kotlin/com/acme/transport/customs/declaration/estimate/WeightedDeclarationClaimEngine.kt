package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the weighted declaration claim calculation. */
interface WeightedDeclarationClaimFactor {
    fun describe(): String
    fun adjust(item: WeightedDeclarationClaim, value: Double): Double
}

data class CapWeightedDeclarationClaimAdjuster(var cap: Double) : WeightedDeclarationClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedDeclarationClaim, value: Double): Double = minOf(value, cap)
}

data class ChannelWeightedDeclarationClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDeclarationClaimFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: WeightedDeclarationClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumWeightedDeclarationClaimAdjuster(var minimum: Double) : WeightedDeclarationClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedDeclarationClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedWeightedDeclarationClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDeclarationClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDeclarationClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedDeclarationClaimQuantityOffsetAdjuster(var weight: Double) : WeightedDeclarationClaimFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: WeightedDeclarationClaim, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the weighted declaration claim steps in order and keeps a trace of each. */
data class WeightedDeclarationClaimEngine(
    var steps: MutableList<WeightedDeclarationClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedDeclarationClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedDeclarationClaimFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedDeclarationClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedDeclarationClaimEngine(flags: Registry): WeightedDeclarationClaimEngine {
    return WeightedDeclarationClaimEngine(
        steps = mutableListOf<WeightedDeclarationClaimFactor>(
            CapWeightedDeclarationClaimAdjuster(48362.0),
            ChannelWeightedDeclarationClaimAdjuster(
                mutableMapOf<String, Double>("retail" to 1.25, "online" to 1.14),
            ),
            MinimumWeightedDeclarationClaimAdjuster(22.0),
            FlaggedWeightedDeclarationClaimAdjuster(flags, "weighted-declaration-claim-manual", 1.6),
            WeightedDeclarationClaimQuantityOffsetAdjuster(0.53),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 6746.0,
    )
}
