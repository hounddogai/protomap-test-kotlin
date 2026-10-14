package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient zone group calculation. */
interface LenientZoneGroupAdjuster {
    fun describe(): String
    fun adjust(item: LenientZoneGroup, value: Double): Double
}

data class MinimumLenientZoneGroupAdjuster(var minimum: Double) : LenientZoneGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientZoneGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CategoryLenientZoneGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientZoneGroupAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: LenientZoneGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientZoneGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientZoneGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientZoneGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient zone group steps in order and keeps a trace of each. */
data class LenientZoneGroupEngine(
    var steps: MutableList<LenientZoneGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientZoneGroup): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientZoneGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientZoneGroupEngine(flags: Registry): LenientZoneGroupEngine {
    return LenientZoneGroupEngine(
        steps = mutableListOf<LenientZoneGroupAdjuster>(
            MinimumLenientZoneGroupAdjuster(32.7),
            CategoryLenientZoneGroupAdjuster(mutableMapOf<String, Double>("central" to 1.15)),
            FlaggedLenientZoneGroupAdjuster(flags, "lenient-zone-group-weighted", 4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 79330.9,
    )
}
