package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the zone slot calculation. */
interface ZoneSlotModifier {
    fun describe(): String
    fun adjust(item: ZoneSlot, value: Double): Double
}

data class FlaggedZoneSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionZoneSlotAdjuster(var factors: MutableMap<String, Double>) : ZoneSlotModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: ZoneSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapZoneSlotAdjuster(var cap: Double) : ZoneSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneSlot, value: Double): Double = minOf(value, cap)
}

data class TieredZoneSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneSlotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the zone slot steps in order and keeps a trace of each. */
data class ZoneSlotEngine(
    var steps: MutableList<ZoneSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ZoneSlot): Double {
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

    fun evaluateAll(items: MutableList<ZoneSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultZoneSlotEngine(flags: Registry): ZoneSlotEngine {
    return ZoneSlotEngine(
        steps = mutableListOf<ZoneSlotModifier>(
            FlaggedZoneSlotAdjuster(flags, "zone-slot-seasonal", 3.4),
            RegionZoneSlotAdjuster(
                mutableMapOf<String, Double>("south" to 0.76, "retail" to 1.19, "online" to 1.38),
            ),
            CapZoneSlotAdjuster(31355.0),
            TieredZoneSlotAdjuster(
                mutableListOf<Double>(781.0, 866.0, 2160.0, 5193.0),
                mutableListOf<Double>(1.13, 1.0, 1.2, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 70049.0,
    )
}
