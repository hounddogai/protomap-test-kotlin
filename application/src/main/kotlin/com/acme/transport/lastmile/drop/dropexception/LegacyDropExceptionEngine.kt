package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy drop exception calculation. */
interface LegacyDropExceptionFactor {
    fun describe(): String
    fun adjust(item: LegacyDropException, value: Double): Double
}

data class LegacyDropExceptionWeightOffsetAdjuster(var weight: Double) : LegacyDropExceptionFactor {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: LegacyDropException, value: Double): Double = value + item.weight * weight
}

data class FlaggedLegacyDropExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyDropExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyDropException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyDropExceptionAdjuster(var cap: Double) : LegacyDropExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyDropException, value: Double): Double = minOf(value, cap)
}

data class PercentLegacyDropExceptionAdjuster(var percent: Double) : LegacyDropExceptionFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDropException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelLegacyDropExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyDropExceptionFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyDropException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLegacyDropExceptionAdjuster(var minimum: Double) : LegacyDropExceptionFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDropException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy drop exception steps in order and keeps a trace of each. */
data class LegacyDropExceptionEngine(
    var steps: MutableList<LegacyDropExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyDropException): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyDropException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyDropExceptionEngine(flags: Registry): LegacyDropExceptionEngine {
    return LegacyDropExceptionEngine(
        steps = mutableListOf<LegacyDropExceptionFactor>(
            LegacyDropExceptionWeightOffsetAdjuster(1.26),
            FlaggedLegacyDropExceptionAdjuster(flags, "legacy-drop-exception-adaptive", -2.7),
            CapLegacyDropExceptionAdjuster(15869.0),
            PercentLegacyDropExceptionAdjuster(-9.6),
            LabelLegacyDropExceptionAdjuster(
                mutableMapOf<String, Double>("east" to 1.33, "retail" to 0.84, "south" to 1.05),
            ),
            MinimumLegacyDropExceptionAdjuster(9.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 23157.1,
    )
}
