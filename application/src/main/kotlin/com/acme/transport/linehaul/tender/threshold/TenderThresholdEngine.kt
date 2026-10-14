package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tender threshold calculation. */
interface TenderThresholdStep {
    fun describe(): String
    fun adjust(item: TenderThreshold, value: Double): Double
}

data class RoundingTenderThresholdAdjuster(var step: Double) : TenderThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderThreshold, value: Double): Double = roundTo(value, step)
}

data class PercentTenderThresholdAdjuster(var percent: Double) : TenderThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTenderThresholdAdjuster(var cap: Double) : TenderThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderThreshold, value: Double): Double = minOf(value, cap)
}

data class NameTenderThresholdAdjuster(var factors: MutableMap<String, Double>) : TenderThresholdStep {
    override fun describe(): String = "by name"

    override fun adjust(item: TenderThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTenderThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderThresholdStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tender threshold steps in order and keeps a trace of each. */
data class TenderThresholdEngine(
    var steps: MutableList<TenderThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TenderThreshold): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<TenderThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTenderThresholdEngine(flags: Registry): TenderThresholdEngine {
    return TenderThresholdEngine(
        steps = mutableListOf<TenderThresholdStep>(
            RoundingTenderThresholdAdjuster(5.0),
            PercentTenderThresholdAdjuster(5.4),
            CapTenderThresholdAdjuster(43575.0),
            NameTenderThresholdAdjuster(mutableMapOf<String, Double>("west" to 1.24, "retail" to 0.84)),
            FlaggedTenderThresholdAdjuster(flags, "tender-threshold-lenient", 7.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 76943.6,
    )
}
