package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default declaration mapping calculation. */
interface DefaultDeclarationMappingModifier {
    fun describe(): String
    fun adjust(item: DefaultDeclarationMapping, value: Double): Double
}

data class FlaggedDefaultDeclarationMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultDeclarationMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultDeclarationMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneDefaultDeclarationMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultDeclarationMappingModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: DefaultDeclarationMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDefaultDeclarationMappingAdjuster(var step: Double) : DefaultDeclarationMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultDeclarationMapping, value: Double): Double = roundTo(value, step)
}

data class CapDefaultDeclarationMappingAdjuster(var cap: Double) : DefaultDeclarationMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultDeclarationMapping, value: Double): Double = minOf(value, cap)
}

data class DefaultDeclarationMappingDurationMinutesOffsetAdjuster(
    var weight: Double,
) : DefaultDeclarationMappingModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: DefaultDeclarationMapping, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class TieredDefaultDeclarationMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultDeclarationMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultDeclarationMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default declaration mapping steps in order and keeps a trace of each. */
data class DefaultDeclarationMappingEngineExtra(
    var steps: MutableList<DefaultDeclarationMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultDeclarationMapping): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDeclarationMappingModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDeclarationMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultDeclarationMappingEngine(flags: Registry): DefaultDeclarationMappingEngineExtra {
    return DefaultDeclarationMappingEngineExtra(
        steps = mutableListOf<DefaultDeclarationMappingModifier>(
            FlaggedDefaultDeclarationMappingAdjuster(flags, "default-declaration-mapping-pending", 7.2),
            LaneDefaultDeclarationMappingAdjuster(mutableMapOf<String, Double>("north" to 0.94)),
            RoundingDefaultDeclarationMappingAdjuster(0.1),
            CapDefaultDeclarationMappingAdjuster(47774.0),
            DefaultDeclarationMappingDurationMinutesOffsetAdjuster(0.35),
            TieredDefaultDeclarationMappingAdjuster(
                mutableListOf<Double>(2303.0, 8478.0),
                mutableListOf<Double>(0.99, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 90082.3,
    )
}
