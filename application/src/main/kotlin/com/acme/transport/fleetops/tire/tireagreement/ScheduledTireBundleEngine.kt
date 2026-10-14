package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled tire bundle calculation. */
interface ScheduledTireBundleAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledTireBundle, value: Double): Double
}

data class CapScheduledTireBundleAdjuster(var cap: Double) : ScheduledTireBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledTireBundle, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledTireBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledTireBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledTireBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledTireBundleMinimumOffsetAdjuster(var weight: Double) : ScheduledTireBundleAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: ScheduledTireBundle, value: Double): Double = value + item.minimum * weight
}

data class RoundingScheduledTireBundleAdjuster(var step: Double) : ScheduledTireBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledTireBundle, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled tire bundle steps in order and keeps a trace of each. */
data class ScheduledTireBundleEngine(
    var steps: MutableList<ScheduledTireBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledTireBundle): Double {
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

    fun evaluateAll(items: MutableList<ScheduledTireBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledTireBundleEngine(flags: Registry): ScheduledTireBundleEngine {
    return ScheduledTireBundleEngine(
        steps = mutableListOf<ScheduledTireBundleAdjuster>(
            CapScheduledTireBundleAdjuster(11718.0),
            FlaggedScheduledTireBundleAdjuster(flags, "scheduled-tire-bundle-pending", -7.1),
            ScheduledTireBundleMinimumOffsetAdjuster(0.4),
            RoundingScheduledTireBundleAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 55736.2,
    )
}
