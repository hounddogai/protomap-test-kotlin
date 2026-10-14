package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary tire override calculation. */
interface PrimaryTireOverrideFactor {
    fun describe(): String
    fun adjust(item: PrimaryTireOverride, value: Double): Double
}

data class FlaggedPrimaryTireOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTireOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTireOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimaryTireOverrideMarginOffsetAdjuster(var weight: Double) : PrimaryTireOverrideFactor {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: PrimaryTireOverride, value: Double): Double = value + item.margin * weight
}

data class MinimumPrimaryTireOverrideAdjuster(var minimum: Double) : PrimaryTireOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTireOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary tire override steps in order and keeps a trace of each. */
data class PrimaryTireOverrideEngine(
    var steps: MutableList<PrimaryTireOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryTireOverride): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryTireOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryTireOverrideEngine(flags: Registry): PrimaryTireOverrideEngine {
    return PrimaryTireOverrideEngine(
        steps = mutableListOf<PrimaryTireOverrideFactor>(
            FlaggedPrimaryTireOverrideAdjuster(flags, "primary-tire-override-manual", -8.7),
            PrimaryTireOverrideMarginOffsetAdjuster(0.58),
            MinimumPrimaryTireOverrideAdjuster(2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 15198.7,
    )
}
