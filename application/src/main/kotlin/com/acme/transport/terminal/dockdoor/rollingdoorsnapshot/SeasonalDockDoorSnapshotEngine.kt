package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal dock door snapshot calculation. */
interface SeasonalDockDoorSnapshotStep {
    fun describe(): String
    fun adjust(item: SeasonalDockDoorSnapshot, value: Double): Double
}

data class TieredSeasonalDockDoorSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDockDoorSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDockDoorSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentSeasonalDockDoorSnapshotAdjuster(var percent: Double) : SeasonalDockDoorSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDockDoorSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSeasonalDockDoorSnapshotAdjuster(var cap: Double) : SeasonalDockDoorSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDockDoorSnapshot, value: Double): Double = minOf(value, cap)
}

data class FlaggedSeasonalDockDoorSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDockDoorSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDockDoorSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal dock door snapshot steps in order and keeps a trace of each. */
data class SeasonalDockDoorSnapshotEngine(
    var steps: MutableList<SeasonalDockDoorSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalDockDoorSnapshot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalDockDoorSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalDockDoorSnapshotEngine(flags: Registry): SeasonalDockDoorSnapshotEngine {
    return SeasonalDockDoorSnapshotEngine(
        steps = mutableListOf<SeasonalDockDoorSnapshotStep>(
            TieredSeasonalDockDoorSnapshotAdjuster(
                mutableListOf<Double>(768.0, 4199.0, 4378.0, 9700.0),
                mutableListOf<Double>(0.96, 0.87, 1.13, 1.07),
            ),
            PercentSeasonalDockDoorSnapshotAdjuster(-13.6),
            CapSeasonalDockDoorSnapshotAdjuster(26852.0),
            FlaggedSeasonalDockDoorSnapshotAdjuster(flags, "seasonal-dock-door-snapshot-batched", -7.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 49098.5,
    )
}
