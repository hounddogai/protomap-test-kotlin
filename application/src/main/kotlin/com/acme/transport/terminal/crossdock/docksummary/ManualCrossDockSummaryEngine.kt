package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual cross dock summary calculation. */
interface ManualCrossDockSummaryAdjuster {
    fun describe(): String
    fun adjust(item: ManualCrossDockSummary, value: Double): Double
}

data class PercentManualCrossDockSummaryAdjuster(var percent: Double) : ManualCrossDockSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualCrossDockSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualCrossDockSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualCrossDockSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualCrossDockSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualCrossDockSummaryAdjuster(var cap: Double) : ManualCrossDockSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualCrossDockSummary, value: Double): Double = minOf(value, cap)
}

data class RoundingManualCrossDockSummaryAdjuster(var step: Double) : ManualCrossDockSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualCrossDockSummary, value: Double): Double = roundTo(value, step)
}

data class ManualCrossDockSummaryStartsAtOffsetAdjuster(var weight: Double) : ManualCrossDockSummaryAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: ManualCrossDockSummary, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

/** Runs the manual cross dock summary steps in order and keeps a trace of each. */
data class ManualCrossDockSummaryEngine(
    var steps: MutableList<ManualCrossDockSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualCrossDockSummary): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualCrossDockSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualCrossDockSummaryEngine(flags: Registry): ManualCrossDockSummaryEngine {
    return ManualCrossDockSummaryEngine(
        steps = mutableListOf<ManualCrossDockSummaryAdjuster>(
            PercentManualCrossDockSummaryAdjuster(23.6),
            FlaggedManualCrossDockSummaryAdjuster(flags, "manual-cross-dock-summary-capped", -2.5),
            CapManualCrossDockSummaryAdjuster(41329.0),
            RoundingManualCrossDockSummaryAdjuster(0.05),
            ManualCrossDockSummaryStartsAtOffsetAdjuster(1.96),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 21349.8,
    )
}
