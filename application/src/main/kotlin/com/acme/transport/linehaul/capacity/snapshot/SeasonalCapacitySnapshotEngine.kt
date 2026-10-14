package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal capacity snapshot calculation. */
interface SeasonalCapacitySnapshotStep {
    fun describe(): String
    fun adjust(item: SeasonalCapacitySnapshot, value: Double): Double
}

data class DepotSeasonalCapacitySnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalCapacitySnapshotStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: SeasonalCapacitySnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSeasonalCapacitySnapshotAdjuster(var cap: Double) : SeasonalCapacitySnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalCapacitySnapshot, value: Double): Double = minOf(value, cap)
}

data class PercentSeasonalCapacitySnapshotAdjuster(var percent: Double) : SeasonalCapacitySnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalCapacitySnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalCapacitySnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalCapacitySnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalCapacitySnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal capacity snapshot steps in order and keeps a trace of each. */
data class SeasonalCapacitySnapshotEngine(
    var steps: MutableList<SeasonalCapacitySnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalCapacitySnapshot): Double {
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

    fun evaluateAll(items: MutableList<SeasonalCapacitySnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalCapacitySnapshotEngine(flags: Registry): SeasonalCapacitySnapshotEngine {
    return SeasonalCapacitySnapshotEngine(
        steps = mutableListOf<SeasonalCapacitySnapshotStep>(
            DepotSeasonalCapacitySnapshotAdjuster(
                mutableMapOf<String, Double>("south" to 0.71, "north" to 1.04, "online" to 1.2),
            ),
            CapSeasonalCapacitySnapshotAdjuster(22256.0),
            PercentSeasonalCapacitySnapshotAdjuster(-24.7),
            FlaggedSeasonalCapacitySnapshotAdjuster(flags, "seasonal-capacity-snapshot-seasonal", 7.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 60023.2,
    )
}
