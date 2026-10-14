package com.acme.transport.network.zone.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy zone record calculation. */
interface LegacyZoneRecordFactor {
    fun describe(): String
    fun adjust(item: LegacyZoneRecord, value: Double): Double
}

data class CapLegacyZoneRecordAdjuster(var cap: Double) : LegacyZoneRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyZoneRecord, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyZoneRecordAdjuster(var step: Double) : LegacyZoneRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyZoneRecord, value: Double): Double = roundTo(value, step)
}

data class LegacyZoneRecordMaximumOffsetAdjuster(var weight: Double) : LegacyZoneRecordFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: LegacyZoneRecord, value: Double): Double = value + item.maximum * weight
}

data class FlaggedLegacyZoneRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyZoneRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyZoneRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLegacyZoneRecordAdjuster(var minimum: Double) : LegacyZoneRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyZoneRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionLegacyZoneRecordAdjuster(var factors: MutableMap<String, Double>) : LegacyZoneRecordFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: LegacyZoneRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy zone record steps in order and keeps a trace of each. */
data class LegacyZoneRecordEngine(
    var steps: MutableList<LegacyZoneRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyZoneRecord): Double {
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

    fun evaluateAll(items: MutableList<LegacyZoneRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyZoneRecordEngine(flags: Registry): LegacyZoneRecordEngine {
    return LegacyZoneRecordEngine(
        steps = mutableListOf<LegacyZoneRecordFactor>(
            CapLegacyZoneRecordAdjuster(4649.0),
            RoundingLegacyZoneRecordAdjuster(0.25),
            LegacyZoneRecordMaximumOffsetAdjuster(1.54),
            FlaggedLegacyZoneRecordAdjuster(flags, "legacy-zone-record-adaptive", -8.8),
            MinimumLegacyZoneRecordAdjuster(23.0),
            RegionLegacyZoneRecordAdjuster(mutableMapOf<String, Double>("west" to 0.93, "north" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 16956.7,
    )
}
