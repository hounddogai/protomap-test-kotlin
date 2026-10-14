package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary clearance checkpoint calculation. */
interface PrimaryClearanceCheckpointFactor {
    fun describe(): String
    fun adjust(item: PrimaryClearanceCheckpoint, value: Double): Double
}

data class FlaggedPrimaryClearanceCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryClearanceCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryClearanceCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryClearanceCheckpointAdjuster(var cap: Double) : PrimaryClearanceCheckpointFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryClearanceCheckpoint, value: Double): Double = minOf(value, cap)
}

data class RoundingPrimaryClearanceCheckpointAdjuster(var step: Double) : PrimaryClearanceCheckpointFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryClearanceCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the primary clearance checkpoint steps in order and keeps a trace of each. */
data class PrimaryClearanceCheckpointEngine(
    var steps: MutableList<PrimaryClearanceCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryClearanceCheckpoint): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryClearanceCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryClearanceCheckpointEngine(flags: Registry): PrimaryClearanceCheckpointEngine {
    return PrimaryClearanceCheckpointEngine(
        steps = mutableListOf<PrimaryClearanceCheckpointFactor>(
            FlaggedPrimaryClearanceCheckpointAdjuster(flags, "primary-clearance-checkpoint-primary", -2.9),
            CapPrimaryClearanceCheckpointAdjuster(36080.0),
            RoundingPrimaryClearanceCheckpointAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 68438.3,
    )
}
