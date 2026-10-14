package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped backhaul profile calculation. */
interface CappedBackhaulProfileStep {
    fun describe(): String
    fun adjust(item: CappedBackhaulProfile, value: Double): Double
}

data class MinimumCappedBackhaulProfileAdjuster(var minimum: Double) : CappedBackhaulProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedBackhaulProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCappedBackhaulProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedBackhaulProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedBackhaulProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedBackhaulProfileAdjuster(var step: Double) : CappedBackhaulProfileStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedBackhaulProfile, value: Double): Double = roundTo(value, step)
}

data class CappedBackhaulProfileLimitOffsetAdjuster(var weight: Double) : CappedBackhaulProfileStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: CappedBackhaulProfile, value: Double): Double = value + item.limit.toDouble() * weight
}

data class CapCappedBackhaulProfileAdjuster(var cap: Double) : CappedBackhaulProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedBackhaulProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the capped backhaul profile steps in order and keeps a trace of each. */
data class CappedBackhaulProfileEngine(
    var steps: MutableList<CappedBackhaulProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedBackhaulProfile): Double {
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

    fun evaluateAll(items: MutableList<CappedBackhaulProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedBackhaulProfileEngine(flags: Registry): CappedBackhaulProfileEngine {
    return CappedBackhaulProfileEngine(
        steps = mutableListOf<CappedBackhaulProfileStep>(
            MinimumCappedBackhaulProfileAdjuster(29.4),
            FlaggedCappedBackhaulProfileAdjuster(flags, "capped-backhaul-profile-manual", -9.7),
            RoundingCappedBackhaulProfileAdjuster(1.0),
            CappedBackhaulProfileLimitOffsetAdjuster(0.92),
            CapCappedBackhaulProfileAdjuster(20693.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 33600.2,
    )
}
