package com.acme.transport.terminal.crossdock.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped cross dock allocation calculation. */
interface CappedCrossDockAllocationStep {
    fun describe(): String
    fun adjust(item: CappedCrossDockAllocation, value: Double): Double
}

data class FlaggedCappedCrossDockAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedCrossDockAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedCrossDockAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedCrossDockAllocationAdjuster(var step: Double) : CappedCrossDockAllocationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedCrossDockAllocation, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedCrossDockAllocationAdjuster(var minimum: Double) : CappedCrossDockAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedCrossDockAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedCrossDockAllocationAdjuster(var percent: Double) : CappedCrossDockAllocationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCrossDockAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelCappedCrossDockAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCrossDockAllocationStep {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedCrossDockAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedCrossDockAllocationAdjuster(var cap: Double) : CappedCrossDockAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCrossDockAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the capped cross dock allocation steps in order and keeps a trace of each. */
data class CappedCrossDockAllocationEngine(
    var steps: MutableList<CappedCrossDockAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedCrossDockAllocation): Double {
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

    fun evaluateAll(items: MutableList<CappedCrossDockAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedCrossDockAllocationEngine(flags: Registry): CappedCrossDockAllocationEngine {
    return CappedCrossDockAllocationEngine(
        steps = mutableListOf<CappedCrossDockAllocationStep>(
            FlaggedCappedCrossDockAllocationAdjuster(flags, "capped-cross-dock-allocation-lenient", -9.8),
            RoundingCappedCrossDockAllocationAdjuster(5.0),
            MinimumCappedCrossDockAllocationAdjuster(36.5),
            PercentCappedCrossDockAllocationAdjuster(-19.6),
            LabelCappedCrossDockAllocationAdjuster(
                mutableMapOf<String, Double>("online" to 0.98, "north" to 1.13),
            ),
            CapCappedCrossDockAllocationAdjuster(32875.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 68027.3,
    )
}
