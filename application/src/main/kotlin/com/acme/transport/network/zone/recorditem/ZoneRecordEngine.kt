package com.acme.transport.network.zone.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the zone record calculation. */
interface ZoneRecordFactor {
    fun describe(): String
    fun adjust(item: ZoneRecord, value: Double): Double
}

data class TieredZoneRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentZoneRecordAdjuster(var percent: Double) : ZoneRecordFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedZoneRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapZoneRecordAdjuster(var cap: Double) : ZoneRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneRecord, value: Double): Double = minOf(value, cap)
}

data class MinimumZoneRecordAdjuster(var minimum: Double) : ZoneRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the zone record steps in order and keeps a trace of each. */
data class ZoneRecordEngine(
    var steps: MutableList<ZoneRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ZoneRecord): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ZoneRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultZoneRecordEngine(flags: Registry): ZoneRecordEngine {
    return ZoneRecordEngine(
        steps = mutableListOf<ZoneRecordFactor>(
            TieredZoneRecordAdjuster(mutableListOf<Double>(3988.0, 6762.0), mutableListOf<Double>(1.04, 0.85)),
            PercentZoneRecordAdjuster(-11.4),
            FlaggedZoneRecordAdjuster(flags, "zone-record-capped", 8.0),
            CapZoneRecordAdjuster(45299.0),
            MinimumZoneRecordAdjuster(38.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 76638.4,
    )
}
