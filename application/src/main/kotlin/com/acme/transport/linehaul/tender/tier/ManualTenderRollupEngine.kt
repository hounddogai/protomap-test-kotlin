package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual tender rollup calculation. */
interface ManualTenderRollupFactor {
    fun describe(): String
    fun adjust(item: ManualTenderRollup, value: Double): Double
}

data class CapManualTenderRollupAdjuster(var cap: Double) : ManualTenderRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTenderRollup, value: Double): Double = minOf(value, cap)
}

data class TieredManualTenderRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTenderRollupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTenderRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualTenderRollupAdjuster(var percent: Double) : ManualTenderRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTenderRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneManualTenderRollupAdjuster(var factors: MutableMap<String, Double>) : ManualTenderRollupFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: ManualTenderRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedManualTenderRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTenderRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTenderRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualTenderRollupAdjuster(var step: Double) : ManualTenderRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTenderRollup, value: Double): Double = roundTo(value, step)
}

/** Runs the manual tender rollup steps in order and keeps a trace of each. */
data class ManualTenderRollupEngine(
    var steps: MutableList<ManualTenderRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualTenderRollup): Double {
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

    fun evaluateAll(items: MutableList<ManualTenderRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualTenderRollupEngine(flags: Registry): ManualTenderRollupEngine {
    return ManualTenderRollupEngine(
        steps = mutableListOf<ManualTenderRollupFactor>(
            CapManualTenderRollupAdjuster(44663.0),
            TieredManualTenderRollupAdjuster(
                mutableListOf<Double>(4470.0, 4590.0, 5434.0),
                mutableListOf<Double>(1.02, 0.88, 1.15),
            ),
            PercentManualTenderRollupAdjuster(3.8),
            LaneManualTenderRollupAdjuster(mutableMapOf<String, Double>("north" to 1.03)),
            FlaggedManualTenderRollupAdjuster(flags, "manual-tender-rollup-rolling", 8.7),
            RoundingManualTenderRollupAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 33047.5,
    )
}
