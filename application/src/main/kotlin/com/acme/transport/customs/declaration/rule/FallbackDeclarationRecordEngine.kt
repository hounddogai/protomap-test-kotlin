package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.flags.Registry

/** One step of the fallback declaration record calculation. */
interface FallbackDeclarationRecordFactor {
    fun describe(): String
    fun adjust(item: FallbackDeclarationRecord, value: Double): Double
}

data class FlaggedFallbackDeclarationRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDeclarationRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDeclarationRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackDeclarationRecordAdjuster(var percent: Double) : FallbackDeclarationRecordFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDeclarationRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryFallbackDeclarationRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackDeclarationRecordFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: FallbackDeclarationRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackDeclarationRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackDeclarationRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackDeclarationRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFallbackDeclarationRecordAdjuster(var minimum: Double) : FallbackDeclarationRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDeclarationRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackDeclarationRecordAdjuster(var cap: Double) : FallbackDeclarationRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDeclarationRecord, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback declaration record steps in order and keeps a trace of each. */
data class FallbackDeclarationRecordEngine(
    var steps: MutableList<FallbackDeclarationRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackDeclarationRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDeclarationRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackDeclarationRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackDeclarationRecordEngine(flags: Registry): FallbackDeclarationRecordEngine {
    return FallbackDeclarationRecordEngine(
        steps = mutableListOf<FallbackDeclarationRecordFactor>(
            FlaggedFallbackDeclarationRecordAdjuster(flags, "fallback-declaration-record-rolling", -3.7),
            PercentFallbackDeclarationRecordAdjuster(-16.3),
            CategoryFallbackDeclarationRecordAdjuster(
                mutableMapOf<String, Double>("central" to 0.74, "west" to 1.17),
            ),
            TieredFallbackDeclarationRecordAdjuster(
                mutableListOf<Double>(2157.0, 4897.0, 6665.0),
                mutableListOf<Double>(1.0, 1.14, 0.9),
            ),
            MinimumFallbackDeclarationRecordAdjuster(13.4),
            CapFallbackDeclarationRecordAdjuster(41890.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 11926.6,
    )
}
