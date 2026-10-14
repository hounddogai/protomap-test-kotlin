package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the zone group calculation. */
interface ZoneGroupAdjuster {
    fun describe(): String
    fun adjust(item: ZoneGroup, value: Double): Double
}

data class CapZoneGroupAdjuster(var cap: Double) : ZoneGroupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneGroup, value: Double): Double = minOf(value, cap)
}

data class ZoneGroupCeilingOffsetAdjuster(var weight: Double) : ZoneGroupAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: ZoneGroup, value: Double): Double = value + item.ceiling * weight
}

data class RoundingZoneGroupAdjuster(var step: Double) : ZoneGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneGroup, value: Double): Double = roundTo(value, step)
}

data class FlaggedZoneGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentZoneGroupAdjuster(var percent: Double) : ZoneGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the zone group steps in order and keeps a trace of each. */
data class ZoneGroupEngine(
    var steps: MutableList<ZoneGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ZoneGroup): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ZoneGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultZoneGroupEngine(flags: Registry): ZoneGroupEngine {
    return ZoneGroupEngine(
        steps = mutableListOf<ZoneGroupAdjuster>(
            CapZoneGroupAdjuster(48898.0),
            ZoneGroupCeilingOffsetAdjuster(0.18),
            RoundingZoneGroupAdjuster(1.0),
            FlaggedZoneGroupAdjuster(flags, "zone-group-capped", -8.1),
            PercentZoneGroupAdjuster(22.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 60534.6,
    )
}
