package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the ev charge record calculation. */
interface EvChargeRecordStep {
    fun describe(): String
    fun adjust(item: EvChargeRecord, value: Double): Double
}

data class RoundingEvChargeRecordAdjuster(var step: Double) : EvChargeRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeRecord, value: Double): Double = roundTo(value, step)
}

data class MinimumEvChargeRecordAdjuster(var minimum: Double) : EvChargeRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredEvChargeRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentEvChargeRecordAdjuster(var percent: Double) : EvChargeRecordStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapEvChargeRecordAdjuster(var cap: Double) : EvChargeRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeRecord, value: Double): Double = minOf(value, cap)
}

data class FlaggedEvChargeRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeRecordStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the ev charge record steps in order and keeps a trace of each. */
data class EvChargeRecordEngine(
    var steps: MutableList<EvChargeRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeRecord): Double {
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

    fun evaluateAll(items: MutableList<EvChargeRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeRecordEngine(flags: Registry): EvChargeRecordEngine {
    return EvChargeRecordEngine(
        steps = mutableListOf<EvChargeRecordStep>(
            RoundingEvChargeRecordAdjuster(0.1),
            MinimumEvChargeRecordAdjuster(47.2),
            TieredEvChargeRecordAdjuster(
                mutableListOf<Double>(2497.0, 2880.0, 9901.0),
                mutableListOf<Double>(0.96, 0.89, 0.83),
            ),
            PercentEvChargeRecordAdjuster(21.9),
            CapEvChargeRecordAdjuster(34671.0),
            FlaggedEvChargeRecordAdjuster(flags, "ev-charge-record-default", -4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 59164.0,
    )
}
