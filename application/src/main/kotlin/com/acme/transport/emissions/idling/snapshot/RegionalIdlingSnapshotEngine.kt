package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional idling snapshot calculation. */
interface RegionalIdlingSnapshotStep {
    fun describe(): String
    fun adjust(item: RegionalIdlingSnapshot, value: Double): Double
}

data class RegionalIdlingSnapshotRateOffsetAdjuster(var weight: Double) : RegionalIdlingSnapshotStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: RegionalIdlingSnapshot, value: Double): Double = value + item.rate * weight
}

data class PercentRegionalIdlingSnapshotAdjuster(var percent: Double) : RegionalIdlingSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalIdlingSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalIdlingSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalIdlingSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalIdlingSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional idling snapshot steps in order and keeps a trace of each. */
data class RegionalIdlingSnapshotEngine(
    var steps: MutableList<RegionalIdlingSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalIdlingSnapshot): Double {
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

    fun evaluateAll(items: MutableList<RegionalIdlingSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalIdlingSnapshotEngine(flags: Registry): RegionalIdlingSnapshotEngine {
    return RegionalIdlingSnapshotEngine(
        steps = mutableListOf<RegionalIdlingSnapshotStep>(
            RegionalIdlingSnapshotRateOffsetAdjuster(1.21),
            PercentRegionalIdlingSnapshotAdjuster(20.8),
            FlaggedRegionalIdlingSnapshotAdjuster(flags, "regional-idling-snapshot-primary", 0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 68781.7,
    )
}
