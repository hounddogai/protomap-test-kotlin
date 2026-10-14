package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual trailer override calculation. */
interface ManualTrailerOverrideAdjuster {
    fun describe(): String
    fun adjust(item: ManualTrailerOverride, value: Double): Double
}

data class FlaggedManualTrailerOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTrailerOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTrailerOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualTrailerOverrideAdjuster(var cap: Double) : ManualTrailerOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTrailerOverride, value: Double): Double = minOf(value, cap)
}

data class PercentManualTrailerOverrideAdjuster(var percent: Double) : ManualTrailerOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTrailerOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumManualTrailerOverrideAdjuster(var minimum: Double) : ManualTrailerOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTrailerOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitManualTrailerOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualTrailerOverrideAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: ManualTrailerOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual trailer override steps in order and keeps a trace of each. */
data class ManualTrailerOverrideEngine(
    var steps: MutableList<ManualTrailerOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualTrailerOverride): Double {
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

    fun evaluateAll(items: MutableList<ManualTrailerOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualTrailerOverrideEngine(flags: Registry): ManualTrailerOverrideEngine {
    return ManualTrailerOverrideEngine(
        steps = mutableListOf<ManualTrailerOverrideAdjuster>(
            FlaggedManualTrailerOverrideAdjuster(flags, "manual-trailer-override-strict", -0.9),
            CapManualTrailerOverrideAdjuster(25056.0),
            PercentManualTrailerOverrideAdjuster(-7.7),
            MinimumManualTrailerOverrideAdjuster(48.5),
            UnitManualTrailerOverrideAdjuster(
                mutableMapOf<String, Double>("north" to 0.75, "online" to 0.87, "south" to 1.31),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 91990.0,
    )
}
