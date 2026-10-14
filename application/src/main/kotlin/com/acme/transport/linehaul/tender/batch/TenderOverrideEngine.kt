package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tender override calculation. */
interface TenderOverrideFactor {
    fun describe(): String
    fun adjust(item: TenderOverride, value: Double): Double
}

data class TenderOverrideDurationMinutesOffsetAdjuster(var weight: Double) : TenderOverrideFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: TenderOverride, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class CapTenderOverrideAdjuster(var cap: Double) : TenderOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderOverride, value: Double): Double = minOf(value, cap)
}

data class MinimumTenderOverrideAdjuster(var minimum: Double) : TenderOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTenderOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryTenderOverrideAdjuster(var factors: MutableMap<String, Double>) : TenderOverrideFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: TenderOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTenderOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TenderOverrideFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TenderOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tender override steps in order and keeps a trace of each. */
data class TenderOverrideEngine(
    var steps: MutableList<TenderOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TenderOverride): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TenderOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTenderOverrideEngine(flags: Registry): TenderOverrideEngine {
    return TenderOverrideEngine(
        steps = mutableListOf<TenderOverrideFactor>(
            TenderOverrideDurationMinutesOffsetAdjuster(1.89),
            CapTenderOverrideAdjuster(18928.0),
            MinimumTenderOverrideAdjuster(26.2),
            FlaggedTenderOverrideAdjuster(flags, "tender-override-capped", -6.9),
            CategoryTenderOverrideAdjuster(mutableMapOf<String, Double>("west" to 0.94, "east" to 1.02)),
            TieredTenderOverrideAdjuster(mutableListOf<Double>(33.0, 8763.0), mutableListOf<Double>(0.86, 0.8)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 89655.7,
    )
}
