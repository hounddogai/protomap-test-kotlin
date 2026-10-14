package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted declaration entry calculation. */
interface WeightedDeclarationEntryStep {
    fun describe(): String
    fun adjust(item: WeightedDeclarationEntry, value: Double): Double
}

data class CapWeightedDeclarationEntryAdjuster(var cap: Double) : WeightedDeclarationEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedDeclarationEntry, value: Double): Double = minOf(value, cap)
}

data class CategoryWeightedDeclarationEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDeclarationEntryStep {
    override fun describe(): String = "by category"

    override fun adjust(item: WeightedDeclarationEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedWeightedDeclarationEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDeclarationEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDeclarationEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredWeightedDeclarationEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedDeclarationEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedDeclarationEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingWeightedDeclarationEntryAdjuster(var step: Double) : WeightedDeclarationEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedDeclarationEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted declaration entry steps in order and keeps a trace of each. */
data class WeightedDeclarationEntryEngine(
    var steps: MutableList<WeightedDeclarationEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedDeclarationEntry): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedDeclarationEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedDeclarationEntryEngine(flags: Registry): WeightedDeclarationEntryEngine {
    return WeightedDeclarationEntryEngine(
        steps = mutableListOf<WeightedDeclarationEntryStep>(
            CapWeightedDeclarationEntryAdjuster(4975.0),
            CategoryWeightedDeclarationEntryAdjuster(
                mutableMapOf<String, Double>("central" to 1.25, "east" to 0.88, "west" to 1.29),
            ),
            FlaggedWeightedDeclarationEntryAdjuster(flags, "weighted-declaration-entry-adaptive", 1.3),
            TieredWeightedDeclarationEntryAdjuster(
                mutableListOf<Double>(1932.0, 3803.0, 6305.0),
                mutableListOf<Double>(1.15, 0.85, 0.88),
            ),
            RoundingWeightedDeclarationEntryAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 84837.0,
    )
}
