package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

/** One step of the primary fuel surcharge draft calculation. */
interface PrimaryFuelSurchargeDraftModifier {
    fun describe(): String
    fun adjust(item: PrimaryFuelSurchargeDraft, value: Double): Double
}

data class CapPrimaryFuelSurchargeDraftAdjuster(var cap: Double) : PrimaryFuelSurchargeDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryFuelSurchargeDraft, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryFuelSurchargeDraftAdjuster(var minimum: Double) : PrimaryFuelSurchargeDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryFuelSurchargeDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryFuelSurchargeDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryFuelSurchargeDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryFuelSurchargeDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NamePrimaryFuelSurchargeDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryFuelSurchargeDraftModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryFuelSurchargeDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary fuel surcharge draft steps in order and keeps a trace of each. */
data class PrimaryFuelSurchargeDraftEngine(
    var steps: MutableList<PrimaryFuelSurchargeDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryFuelSurchargeDraft): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryFuelSurchargeDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryFuelSurchargeDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryFuelSurchargeDraftEngine(): PrimaryFuelSurchargeDraftEngine {
    return PrimaryFuelSurchargeDraftEngine(
        steps = mutableListOf<PrimaryFuelSurchargeDraftModifier>(
            CapPrimaryFuelSurchargeDraftAdjuster(21648.0),
            MinimumPrimaryFuelSurchargeDraftAdjuster(49.9),
            TieredPrimaryFuelSurchargeDraftAdjuster(
                mutableListOf<Double>(273.0, 4255.0, 8175.0),
                mutableListOf<Double>(1.18, 1.09, 1.01),
            ),
            NamePrimaryFuelSurchargeDraftAdjuster(mutableMapOf<String, Double>("central" to 0.98)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 71857.8,
    )
}
