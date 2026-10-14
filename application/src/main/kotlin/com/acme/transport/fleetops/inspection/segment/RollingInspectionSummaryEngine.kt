package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling inspection summary calculation. */
interface RollingInspectionSummaryFactor {
    fun describe(): String
    fun adjust(item: RollingInspectionSummary, value: Double): Double
}

data class TitleRollingInspectionSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingInspectionSummaryFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingInspectionSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRollingInspectionSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingInspectionSummaryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingInspectionSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRollingInspectionSummaryAdjuster(var step: Double) : RollingInspectionSummaryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingInspectionSummary, value: Double): Double = roundTo(value, step)
}

data class TieredRollingInspectionSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingInspectionSummaryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingInspectionSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling inspection summary steps in order and keeps a trace of each. */
data class RollingInspectionSummaryEngine(
    var steps: MutableList<RollingInspectionSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingInspectionSummary): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingInspectionSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingInspectionSummaryEngine(flags: Registry): RollingInspectionSummaryEngine {
    return RollingInspectionSummaryEngine(
        steps = mutableListOf<RollingInspectionSummaryFactor>(
            TitleRollingInspectionSummaryAdjuster(mutableMapOf<String, Double>("central" to 1.16)),
            FlaggedRollingInspectionSummaryAdjuster(flags, "rolling-inspection-summary-scheduled", -4.2),
            RoundingRollingInspectionSummaryAdjuster(0.1),
            TieredRollingInspectionSummaryAdjuster(
                mutableListOf<Double>(1936.0, 2872.0),
                mutableListOf<Double>(1.03, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 37853.8,
    )
}
