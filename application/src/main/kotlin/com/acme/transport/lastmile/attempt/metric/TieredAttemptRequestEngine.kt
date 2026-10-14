package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered attempt request calculation. */
interface TieredAttemptRequestModifier {
    fun describe(): String
    fun adjust(item: TieredAttemptRequest, value: Double): Double
}

data class CapTieredAttemptRequestAdjuster(var cap: Double) : TieredAttemptRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredAttemptRequest, value: Double): Double = minOf(value, cap)
}

data class TieredAttemptRequestUpdatedAtOffsetAdjuster(var weight: Double) : TieredAttemptRequestModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: TieredAttemptRequest, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class LabelTieredAttemptRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredAttemptRequestModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredAttemptRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTieredAttemptRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredAttemptRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredAttemptRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTieredAttemptRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredAttemptRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredAttemptRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTieredAttemptRequestAdjuster(var percent: Double) : TieredAttemptRequestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredAttemptRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered attempt request steps in order and keeps a trace of each. */
data class TieredAttemptRequestEngine(
    var steps: MutableList<TieredAttemptRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredAttemptRequest): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredAttemptRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredAttemptRequestEngine(flags: Registry): TieredAttemptRequestEngine {
    return TieredAttemptRequestEngine(
        steps = mutableListOf<TieredAttemptRequestModifier>(
            CapTieredAttemptRequestAdjuster(1769.0),
            TieredAttemptRequestUpdatedAtOffsetAdjuster(1.04),
            LabelTieredAttemptRequestAdjuster(mutableMapOf<String, Double>("west" to 1.14)),
            FlaggedTieredAttemptRequestAdjuster(flags, "tiered-attempt-request-primary", -9.3),
            TieredTieredAttemptRequestAdjuster(
                mutableListOf<Double>(1585.0, 2604.0, 8608.0, 9089.0),
                mutableListOf<Double>(1.13, 0.91, 1.05, 0.87),
            ),
            PercentTieredAttemptRequestAdjuster(19.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 46995.2,
    )
}
