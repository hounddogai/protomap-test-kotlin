package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the drop estimate calculation. */
interface DropEstimateAdjuster {
    fun describe(): String
    fun adjust(item: DropEstimate, value: Double): Double
}

data class PercentDropEstimateAdjuster(var percent: Double) : DropEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDropEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelDropEstimateAdjuster(var factors: MutableMap<String, Double>) : DropEstimateAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: DropEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDropEstimateAdjuster(var cap: Double) : DropEstimateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropEstimate, value: Double): Double = minOf(value, cap)
}

data class RoundingDropEstimateAdjuster(var step: Double) : DropEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the drop estimate steps in order and keeps a trace of each. */
data class DropEstimateEngine(
    var steps: MutableList<DropEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DropEstimate): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DropEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDropEstimateEngine(flags: Registry): DropEstimateEngine {
    return DropEstimateEngine(
        steps = mutableListOf<DropEstimateAdjuster>(
            PercentDropEstimateAdjuster(16.4),
            FlaggedDropEstimateAdjuster(flags, "drop-estimate-weighted", -0.7),
            LabelDropEstimateAdjuster(mutableMapOf<String, Double>("retail" to 0.91, "east" to 1.33)),
            CapDropEstimateAdjuster(37143.0),
            RoundingDropEstimateAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 46177.5,
    )
}
