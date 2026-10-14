package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped swap record calculation. */
interface CappedSwapRecordAdjuster {
    fun describe(): String
    fun adjust(item: CappedSwapRecord, value: Double): Double
}

data class FlaggedCappedSwapRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedSwapRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedSwapRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCappedSwapRecordAdjuster(var percent: Double) : CappedSwapRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedSwapRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCappedSwapRecordAdjuster(var cap: Double) : CappedSwapRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedSwapRecord, value: Double): Double = minOf(value, cap)
}

data class RoundingCappedSwapRecordAdjuster(var step: Double) : CappedSwapRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedSwapRecord, value: Double): Double = roundTo(value, step)
}

data class CappedSwapRecordSequenceOffsetAdjuster(var weight: Double) : CappedSwapRecordAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: CappedSwapRecord, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class TieredCappedSwapRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedSwapRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedSwapRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped swap record steps in order and keeps a trace of each. */
data class CappedSwapRecordEngine(
    var steps: MutableList<CappedSwapRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedSwapRecord): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedSwapRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedSwapRecordEngine(flags: Registry): CappedSwapRecordEngine {
    return CappedSwapRecordEngine(
        steps = mutableListOf<CappedSwapRecordAdjuster>(
            FlaggedCappedSwapRecordAdjuster(flags, "capped-swap-record-default", 4.0),
            PercentCappedSwapRecordAdjuster(-15.1),
            CapCappedSwapRecordAdjuster(9719.0),
            RoundingCappedSwapRecordAdjuster(0.1),
            CappedSwapRecordSequenceOffsetAdjuster(0.61),
            TieredCappedSwapRecordAdjuster(
                mutableListOf<Double>(3588.0, 4713.0),
                mutableListOf<Double>(1.0, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 43274.9,
    )
}
