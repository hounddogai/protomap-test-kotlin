package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered dock door target calculation. */
interface TieredDockDoorTargetModifier {
    fun describe(): String
    fun adjust(item: TieredDockDoorTarget, value: Double): Double
}

data class MinimumTieredDockDoorTargetAdjuster(var minimum: Double) : TieredDockDoorTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDockDoorTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTieredDockDoorTargetAdjuster(var cap: Double) : TieredDockDoorTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredDockDoorTarget, value: Double): Double = minOf(value, cap)
}

data class FlaggedTieredDockDoorTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredDockDoorTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredDockDoorTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDockDoorTargetPriorityOffsetAdjuster(var weight: Double) : TieredDockDoorTargetModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: TieredDockDoorTarget, value: Double): Double = value + item.priority.toDouble() * weight
}

data class RoundingTieredDockDoorTargetAdjuster(var step: Double) : TieredDockDoorTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredDockDoorTarget, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered dock door target steps in order and keeps a trace of each. */
data class TieredDockDoorTargetEngine(
    var steps: MutableList<TieredDockDoorTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredDockDoorTarget): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredDockDoorTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredDockDoorTargetEngine(flags: Registry): TieredDockDoorTargetEngine {
    return TieredDockDoorTargetEngine(
        steps = mutableListOf<TieredDockDoorTargetModifier>(
            MinimumTieredDockDoorTargetAdjuster(41.0),
            CapTieredDockDoorTargetAdjuster(16443.0),
            FlaggedTieredDockDoorTargetAdjuster(flags, "tiered-dock-door-target-manual", -0.9),
            TieredDockDoorTargetPriorityOffsetAdjuster(0.61),
            RoundingTieredDockDoorTargetAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 80526.6,
    )
}
