package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the drop group calculation. */
interface DropGroupModifier {
    fun describe(): String
    fun adjust(item: DropGroup, value: Double): Double
}

data class PercentDropGroupAdjuster(var percent: Double) : DropGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDropGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDropGroupAdjuster(var cap: Double) : DropGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropGroup, value: Double): Double = minOf(value, cap)
}

data class DropGroupFloorValueOffsetAdjuster(var weight: Double) : DropGroupModifier {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: DropGroup, value: Double): Double = value + item.floorValue * weight
}

/** Runs the drop group steps in order and keeps a trace of each. */
data class DropGroupEngine(
    var steps: MutableList<DropGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DropGroup): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DropGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDropGroupEngine(flags: Registry): DropGroupEngine {
    return DropGroupEngine(
        steps = mutableListOf<DropGroupModifier>(
            PercentDropGroupAdjuster(3.7),
            FlaggedDropGroupAdjuster(flags, "drop-group-seasonal", 4.0),
            CapDropGroupAdjuster(43932.0),
            DropGroupFloorValueOffsetAdjuster(1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 57544.3,
    )
}
