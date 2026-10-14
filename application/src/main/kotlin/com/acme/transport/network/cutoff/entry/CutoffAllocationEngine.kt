package com.acme.transport.network.cutoff.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cutoff allocation calculation. */
interface CutoffAllocationModifier {
    fun describe(): String
    fun adjust(item: CutoffAllocation, value: Double): Double
}

data class FlaggedCutoffAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCutoffAllocationAdjuster(var cap: Double) : CutoffAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffAllocation, value: Double): Double = minOf(value, cap)
}

data class NameCutoffAllocationAdjuster(var factors: MutableMap<String, Double>) : CutoffAllocationModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: CutoffAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCutoffAllocationAdjuster(var step: Double) : CutoffAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffAllocation, value: Double): Double = roundTo(value, step)
}

/** Runs the cutoff allocation steps in order and keeps a trace of each. */
data class CutoffAllocationEngine(
    var steps: MutableList<CutoffAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CutoffAllocation): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CutoffAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCutoffAllocationEngine(flags: Registry): CutoffAllocationEngine {
    return CutoffAllocationEngine(
        steps = mutableListOf<CutoffAllocationModifier>(
            FlaggedCutoffAllocationAdjuster(flags, "cutoff-allocation-default", -0.4),
            CapCutoffAllocationAdjuster(39461.0),
            NameCutoffAllocationAdjuster(
                mutableMapOf<String, Double>("south" to 1.19, "east" to 1.09, "north" to 1.29),
            ),
            RoundingCutoffAllocationAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 96411.0,
    )
}
