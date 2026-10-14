package com.acme.transport.network.transittime.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy transit time checkpoint calculation. */
interface LegacyTransitTimeCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: LegacyTransitTimeCheckpoint, value: Double): Double
}

data class LegacyTransitTimeCheckpointScoreOffsetAdjuster(
    var weight: Double,
) : LegacyTransitTimeCheckpointAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: LegacyTransitTimeCheckpoint, value: Double): Double = value + item.score * weight
}

data class LabelLegacyTransitTimeCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyTransitTimeCheckpointAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyTransitTimeCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyTransitTimeCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTransitTimeCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTransitTimeCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy transit time checkpoint steps in order and keeps a trace of each. */
data class LegacyTransitTimeCheckpointEngine(
    var steps: MutableList<LegacyTransitTimeCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyTransitTimeCheckpoint): Double {
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

    fun evaluateAll(items: MutableList<LegacyTransitTimeCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyTransitTimeCheckpointEngine(flags: Registry): LegacyTransitTimeCheckpointEngine {
    return LegacyTransitTimeCheckpointEngine(
        steps = mutableListOf<LegacyTransitTimeCheckpointAdjuster>(
            LegacyTransitTimeCheckpointScoreOffsetAdjuster(1.13),
            LabelLegacyTransitTimeCheckpointAdjuster(mutableMapOf<String, Double>("online" to 0.89)),
            FlaggedLegacyTransitTimeCheckpointAdjuster(flags, "legacy-transit-time-checkpoint-tiered", -4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 95735.7,
    )
}
