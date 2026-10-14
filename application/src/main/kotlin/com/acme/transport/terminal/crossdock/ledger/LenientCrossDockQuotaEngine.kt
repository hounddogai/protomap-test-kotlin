package com.acme.transport.terminal.crossdock.ledger

/** One step of the lenient cross dock quota calculation. */
interface LenientCrossDockQuotaModifier {
    fun describe(): String
    fun adjust(item: LenientCrossDockQuota, value: Double): Double
}

data class LenientCrossDockQuotaPalletsOffsetAdjuster(var weight: Double) : LenientCrossDockQuotaModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: LenientCrossDockQuota, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class RegionLenientCrossDockQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientCrossDockQuotaModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: LenientCrossDockQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLenientCrossDockQuotaAdjuster(var cap: Double) : LenientCrossDockQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCrossDockQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient cross dock quota steps in order and keeps a trace of each. */
data class LenientCrossDockQuotaEngine(
    var steps: MutableList<LenientCrossDockQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientCrossDockQuota): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientCrossDockQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientCrossDockQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientCrossDockQuotaEngine(): LenientCrossDockQuotaEngine {
    return LenientCrossDockQuotaEngine(
        steps = mutableListOf<LenientCrossDockQuotaModifier>(
            LenientCrossDockQuotaPalletsOffsetAdjuster(0.13),
            RegionLenientCrossDockQuotaAdjuster(mutableMapOf<String, Double>("retail" to 0.9)),
            CapLenientCrossDockQuotaAdjuster(32632.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 48097.6,
    )
}
