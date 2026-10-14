package com.acme.transport.crewing.swap.swapclaim

import com.acme.transport.kit.flags.Registry

/** One step of the primary swap mapping calculation. */
interface PrimarySwapMappingModifier {
    fun describe(): String
    fun adjust(item: PrimarySwapMapping, value: Double): Double
}

data class TitlePrimarySwapMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimarySwapMappingModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PrimarySwapMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPrimarySwapMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimarySwapMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimarySwapMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimarySwapMappingMaximumOffsetAdjuster(var weight: Double) : PrimarySwapMappingModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: PrimarySwapMapping, value: Double): Double = value + item.maximum * weight
}

data class CapPrimarySwapMappingAdjuster(var cap: Double) : PrimarySwapMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimarySwapMapping, value: Double): Double = minOf(value, cap)
}

data class PercentPrimarySwapMappingAdjuster(var percent: Double) : PrimarySwapMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimarySwapMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary swap mapping steps in order and keeps a trace of each. */
data class PrimarySwapMappingEngine(
    var steps: MutableList<PrimarySwapMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimarySwapMapping): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimarySwapMappingModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimarySwapMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimarySwapMappingEngine(flags: Registry): PrimarySwapMappingEngine {
    return PrimarySwapMappingEngine(
        steps = mutableListOf<PrimarySwapMappingModifier>(
            TitlePrimarySwapMappingAdjuster(mutableMapOf<String, Double>("online" to 1.06)),
            FlaggedPrimarySwapMappingAdjuster(flags, "primary-swap-mapping-default", -2.9),
            PrimarySwapMappingMaximumOffsetAdjuster(1.63),
            CapPrimarySwapMappingAdjuster(46819.0),
            PercentPrimarySwapMappingAdjuster(20.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 56218.7,
    )
}
