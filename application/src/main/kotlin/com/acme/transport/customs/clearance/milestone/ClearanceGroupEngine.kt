package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the clearance group calculation. */
interface ClearanceGroupModifier {
    fun describe(): String
    fun adjust(item: ClearanceGroup, value: Double): Double
}

data class RoundingClearanceGroupAdjuster(var step: Double) : ClearanceGroupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceGroup, value: Double): Double = roundTo(value, step)
}

data class NameClearanceGroupAdjuster(var factors: MutableMap<String, Double>) : ClearanceGroupModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: ClearanceGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapClearanceGroupAdjuster(var cap: Double) : ClearanceGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceGroup, value: Double): Double = minOf(value, cap)
}

data class PercentClearanceGroupAdjuster(var percent: Double) : ClearanceGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredClearanceGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedClearanceGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the clearance group steps in order and keeps a trace of each. */
data class ClearanceGroupEngine(
    var steps: MutableList<ClearanceGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceGroup): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceGroupEngine(flags: Registry): ClearanceGroupEngine {
    return ClearanceGroupEngine(
        steps = mutableListOf<ClearanceGroupModifier>(
            RoundingClearanceGroupAdjuster(5.0),
            NameClearanceGroupAdjuster(
                mutableMapOf<String, Double>("retail" to 1.17, "central" to 1.29, "south" to 1.01),
            ),
            CapClearanceGroupAdjuster(25343.0),
            PercentClearanceGroupAdjuster(20.8),
            TieredClearanceGroupAdjuster(
                mutableListOf<Double>(174.0, 450.0, 2071.0, 4145.0),
                mutableListOf<Double>(1.13, 1.13, 0.94, 0.95),
            ),
            FlaggedClearanceGroupAdjuster(flags, "clearance-group-manual", 4.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 48146.0,
    )
}
