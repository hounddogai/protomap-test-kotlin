package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the violation mapping calculation. */
interface ViolationMappingStep {
    fun describe(): String
    fun adjust(item: ViolationMapping, value: Double): Double
}

data class MinimumViolationMappingAdjuster(var minimum: Double) : ViolationMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapViolationMappingAdjuster(var cap: Double) : ViolationMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationMapping, value: Double): Double = minOf(value, cap)
}

data class TieredViolationMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedViolationMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameViolationMappingAdjuster(var factors: MutableMap<String, Double>) : ViolationMappingStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ViolationMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the violation mapping steps in order and keeps a trace of each. */
data class ViolationMappingEngine(
    var steps: MutableList<ViolationMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ViolationMapping): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationMappingStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultViolationMappingEngine(flags: Registry): ViolationMappingEngine {
    return ViolationMappingEngine(
        steps = mutableListOf<ViolationMappingStep>(
            MinimumViolationMappingAdjuster(41.6),
            CapViolationMappingAdjuster(4513.0),
            TieredViolationMappingAdjuster(
                mutableListOf<Double>(3092.0, 4626.0, 5988.0, 8671.0),
                mutableListOf<Double>(1.15, 0.84, 0.93, 0.97),
            ),
            FlaggedViolationMappingAdjuster(flags, "violation-mapping-primary", -9.1),
            NameViolationMappingAdjuster(mutableMapOf<String, Double>("west" to 1.21)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 80205.3,
    )
}
