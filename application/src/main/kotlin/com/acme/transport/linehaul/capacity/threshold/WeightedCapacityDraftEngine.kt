package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted capacity draft calculation. */
interface WeightedCapacityDraftStep {
    fun describe(): String
    fun adjust(item: WeightedCapacityDraft, value: Double): Double
}

data class FlaggedWeightedCapacityDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedCapacityDraftStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedCapacityDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentWeightedCapacityDraftAdjuster(var percent: Double) : WeightedCapacityDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedCapacityDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleWeightedCapacityDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedCapacityDraftStep {
    override fun describe(): String = "by title"

    override fun adjust(item: WeightedCapacityDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedCapacityDraftAdjuster(var step: Double) : WeightedCapacityDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedCapacityDraft, value: Double): Double = roundTo(value, step)
}

data class WeightedCapacityDraftStopCountOffsetAdjuster(var weight: Double) : WeightedCapacityDraftStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: WeightedCapacityDraft, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class CapWeightedCapacityDraftAdjuster(var cap: Double) : WeightedCapacityDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedCapacityDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted capacity draft steps in order and keeps a trace of each. */
data class WeightedCapacityDraftEngine(
    var steps: MutableList<WeightedCapacityDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedCapacityDraft): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedCapacityDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedCapacityDraftEngine(flags: Registry): WeightedCapacityDraftEngine {
    return WeightedCapacityDraftEngine(
        steps = mutableListOf<WeightedCapacityDraftStep>(
            FlaggedWeightedCapacityDraftAdjuster(flags, "weighted-capacity-draft-seasonal", -4.3),
            PercentWeightedCapacityDraftAdjuster(2.4),
            TitleWeightedCapacityDraftAdjuster(mutableMapOf<String, Double>("north" to 0.8, "online" to 1.29)),
            RoundingWeightedCapacityDraftAdjuster(0.1),
            WeightedCapacityDraftStopCountOffsetAdjuster(0.69),
            CapWeightedCapacityDraftAdjuster(29516.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 87286.9,
    )
}
