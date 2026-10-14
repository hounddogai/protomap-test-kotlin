package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary parcel cycle calculation. */
interface PrimaryParcelCycleAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryParcelCycle, value: Double): Double
}

data class CapPrimaryParcelCycleAdjuster(var cap: Double) : PrimaryParcelCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryParcelCycle, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryParcelCycleAdjuster(var minimum: Double) : PrimaryParcelCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryParcelCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPrimaryParcelCycleAdjuster(var percent: Double) : PrimaryParcelCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryParcelCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NamePrimaryParcelCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryParcelCycleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryParcelCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPrimaryParcelCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryParcelCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryParcelCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary parcel cycle steps in order and keeps a trace of each. */
data class PrimaryParcelCycleEngine(
    var steps: MutableList<PrimaryParcelCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryParcelCycle): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryParcelCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryParcelCycleEngine(flags: Registry): PrimaryParcelCycleEngine {
    return PrimaryParcelCycleEngine(
        steps = mutableListOf<PrimaryParcelCycleAdjuster>(
            CapPrimaryParcelCycleAdjuster(10275.0),
            MinimumPrimaryParcelCycleAdjuster(23.1),
            PercentPrimaryParcelCycleAdjuster(-2.9),
            NamePrimaryParcelCycleAdjuster(mutableMapOf<String, Double>("north" to 0.85)),
            FlaggedPrimaryParcelCycleAdjuster(flags, "primary-parcel-cycle-regional", 7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 97247.5,
    )
}
