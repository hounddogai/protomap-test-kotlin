package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the backhaul profile calculation. */
interface BackhaulProfileAdjuster {
    fun describe(): String
    fun adjust(item: BackhaulProfile, value: Double): Double
}

data class MinimumBackhaulProfileAdjuster(var minimum: Double) : BackhaulProfileAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BackhaulProfileCeilingOffsetAdjuster(var weight: Double) : BackhaulProfileAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: BackhaulProfile, value: Double): Double = value + item.ceiling * weight
}

data class PercentBackhaulProfileAdjuster(var percent: Double) : BackhaulProfileAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBackhaulProfileAdjuster(var cap: Double) : BackhaulProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulProfile, value: Double): Double = minOf(value, cap)
}

data class FlaggedBackhaulProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the backhaul profile steps in order and keeps a trace of each. */
data class BackhaulProfileEngine(
    var steps: MutableList<BackhaulProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulProfile): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulProfileEngine(flags: Registry): BackhaulProfileEngine {
    return BackhaulProfileEngine(
        steps = mutableListOf<BackhaulProfileAdjuster>(
            MinimumBackhaulProfileAdjuster(37.8),
            BackhaulProfileCeilingOffsetAdjuster(2.0),
            PercentBackhaulProfileAdjuster(18.4),
            CapBackhaulProfileAdjuster(11462.0),
            FlaggedBackhaulProfileAdjuster(flags, "backhaul-profile-pending", -7.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 59809.5,
    )
}
