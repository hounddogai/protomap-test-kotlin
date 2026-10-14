package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy hold exception calculation. */
interface LegacyHoldExceptionFactor {
    fun describe(): String
    fun adjust(item: LegacyHoldException, value: Double): Double
}

data class CapLegacyHoldExceptionAdjuster(var cap: Double) : LegacyHoldExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyHoldException, value: Double): Double = minOf(value, cap)
}

data class FlaggedLegacyHoldExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyHoldExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyHoldException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyHoldExceptionAdjuster(var percent: Double) : LegacyHoldExceptionFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyHoldException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLegacyHoldExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyHoldExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyHoldException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy hold exception steps in order and keeps a trace of each. */
data class LegacyHoldExceptionEngine(
    var steps: MutableList<LegacyHoldExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyHoldException): Double {
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

    fun evaluateAll(items: MutableList<LegacyHoldException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyHoldExceptionEngine(flags: Registry): LegacyHoldExceptionEngine {
    return LegacyHoldExceptionEngine(
        steps = mutableListOf<LegacyHoldExceptionFactor>(
            CapLegacyHoldExceptionAdjuster(40772.0),
            FlaggedLegacyHoldExceptionAdjuster(flags, "legacy-hold-exception-weighted", -1.8),
            PercentLegacyHoldExceptionAdjuster(4.8),
            TieredLegacyHoldExceptionAdjuster(
                mutableListOf<Double>(1336.0, 1342.0, 9582.0),
                mutableListOf<Double>(1.19, 1.16, 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 40146.2,
    )
}
