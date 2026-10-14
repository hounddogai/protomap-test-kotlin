package com.acme.transport.lastmile.parcel.overrideitem

/** One step of the legacy parcel mapping calculation. */
interface LegacyParcelMappingStep {
    fun describe(): String
    fun adjust(item: LegacyParcelMapping, value: Double): Double
}

data class LegacyParcelMappingPriorityOffsetAdjuster(var weight: Double) : LegacyParcelMappingStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: LegacyParcelMapping, value: Double): Double = value + item.priority.toDouble() * weight
}

data class DepotLegacyParcelMappingAdjuster(var factors: MutableMap<String, Double>) : LegacyParcelMappingStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: LegacyParcelMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLegacyParcelMappingAdjuster(var cap: Double) : LegacyParcelMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyParcelMapping, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyParcelMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyParcelMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyParcelMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy parcel mapping steps in order and keeps a trace of each. */
data class LegacyParcelMappingEngine(
    var steps: MutableList<LegacyParcelMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyParcelMapping): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyParcelMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyParcelMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyParcelMappingEngine(): LegacyParcelMappingEngine {
    return LegacyParcelMappingEngine(
        steps = mutableListOf<LegacyParcelMappingStep>(
            LegacyParcelMappingPriorityOffsetAdjuster(1.9),
            DepotLegacyParcelMappingAdjuster(mutableMapOf<String, Double>("south" to 0.71)),
            CapLegacyParcelMappingAdjuster(18767.0),
            TieredLegacyParcelMappingAdjuster(
                mutableListOf<Double>(3668.0, 7326.0, 8265.0),
                mutableListOf<Double>(1.2, 0.89, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 83591.8,
    )
}
