package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the utilization draft calculation. */
interface UtilizationDraftFactor {
    fun describe(): String
    fun adjust(item: UtilizationDraft, value: Double): Double
}

data class PercentUtilizationDraftAdjuster(var percent: Double) : UtilizationDraftFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapUtilizationDraftAdjuster(var cap: Double) : UtilizationDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationDraft, value: Double): Double = minOf(value, cap)
}

data class RoundingUtilizationDraftAdjuster(var step: Double) : UtilizationDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationDraft, value: Double): Double = roundTo(value, step)
}

data class FlaggedUtilizationDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationDraftFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelUtilizationDraftAdjuster(var factors: MutableMap<String, Double>) : UtilizationDraftFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: UtilizationDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the utilization draft steps in order and keeps a trace of each. */
data class UtilizationDraftEngine(
    var steps: MutableList<UtilizationDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: UtilizationDraft): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultUtilizationDraftEngine(flags: Registry): UtilizationDraftEngine {
    return UtilizationDraftEngine(
        steps = mutableListOf<UtilizationDraftFactor>(
            PercentUtilizationDraftAdjuster(-0.9),
            CapUtilizationDraftAdjuster(30079.0),
            RoundingUtilizationDraftAdjuster(1.0),
            FlaggedUtilizationDraftAdjuster(flags, "utilization-draft-legacy", -1.5),
            LabelUtilizationDraftAdjuster(
                mutableMapOf<String, Double>("east" to 0.73, "retail" to 0.91, "online" to 1.21),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 92865.9,
    )
}
