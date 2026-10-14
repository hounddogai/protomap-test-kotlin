package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lane plan item calculation. */
interface LanePlanItemStep {
    fun describe(): String
    fun adjust(item: LanePlanItem, value: Double): Double
}

data class TieredLanePlanItemAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanItemStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanItem, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentLanePlanItemAdjuster(var factors: MutableMap<String, Double>) : LanePlanItemStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: LanePlanItem, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLanePlanItemAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanItemStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanItem, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLanePlanItemAdjuster(var step: Double) : LanePlanItemStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanItem, value: Double): Double = roundTo(value, step)
}

/** Runs the lane plan item steps in order and keeps a trace of each. */
data class LanePlanItemEngine(
    var steps: MutableList<LanePlanItemStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LanePlanItem): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanItemStep = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanItem>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLanePlanItemEngine(flags: Registry): LanePlanItemEngine {
    return LanePlanItemEngine(
        steps = mutableListOf<LanePlanItemStep>(
            TieredLanePlanItemAdjuster(
                mutableListOf<Double>(985.0, 9763.0, 9781.0),
                mutableListOf<Double>(1.08, 1.04, 0.9),
            ),
            SegmentLanePlanItemAdjuster(mutableMapOf<String, Double>("central" to 1.07, "west" to 1.24)),
            FlaggedLanePlanItemAdjuster(flags, "lane-plan-item-lenient", 4.0),
            RoundingLanePlanItemAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 4290.2,
    )
}
