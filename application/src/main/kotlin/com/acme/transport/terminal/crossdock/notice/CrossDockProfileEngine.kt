package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the cross dock profile calculation. */
interface CrossDockProfileStep {
    fun describe(): String
    fun adjust(item: CrossDockProfile, value: Double): Double
}

data class FlaggedCrossDockProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCrossDockProfileAdjuster(var percent: Double) : CrossDockProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CrossDockProfileFuelLitersOffsetAdjuster(var weight: Double) : CrossDockProfileStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: CrossDockProfile, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the cross dock profile steps in order and keeps a trace of each. */
data class CrossDockProfileEngine(
    var steps: MutableList<CrossDockProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CrossDockProfile): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCrossDockProfileEngine(flags: Registry): CrossDockProfileEngine {
    return CrossDockProfileEngine(
        steps = mutableListOf<CrossDockProfileStep>(
            FlaggedCrossDockProfileAdjuster(flags, "cross-dock-profile-primary", 5.1),
            PercentCrossDockProfileAdjuster(-18.3),
            CrossDockProfileFuelLitersOffsetAdjuster(0.32),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 51675.9,
    )
}
