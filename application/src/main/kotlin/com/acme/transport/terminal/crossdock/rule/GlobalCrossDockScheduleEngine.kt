package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global cross dock schedule calculation. */
interface GlobalCrossDockScheduleModifier {
    fun describe(): String
    fun adjust(item: GlobalCrossDockSchedule, value: Double): Double
}

data class GlobalCrossDockScheduleGraceDaysOffsetAdjuster(
    var weight: Double,
) : GlobalCrossDockScheduleModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: GlobalCrossDockSchedule, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class PercentGlobalCrossDockScheduleAdjuster(var percent: Double) : GlobalCrossDockScheduleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCrossDockSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalCrossDockScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalCrossDockScheduleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalCrossDockSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global cross dock schedule steps in order and keeps a trace of each. */
data class GlobalCrossDockScheduleEngine(
    var steps: MutableList<GlobalCrossDockScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalCrossDockSchedule): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalCrossDockSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalCrossDockScheduleEngine(flags: Registry): GlobalCrossDockScheduleEngine {
    return GlobalCrossDockScheduleEngine(
        steps = mutableListOf<GlobalCrossDockScheduleModifier>(
            GlobalCrossDockScheduleGraceDaysOffsetAdjuster(0.48),
            PercentGlobalCrossDockScheduleAdjuster(-21.3),
            FlaggedGlobalCrossDockScheduleAdjuster(flags, "global-cross-dock-schedule-regional", 2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 91754.0,
    )
}
