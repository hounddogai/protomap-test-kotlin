package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global near miss record calculation. */
interface GlobalNearMissRecordStep {
    fun describe(): String
    fun adjust(item: GlobalNearMissRecord, value: Double): Double
}

data class CapGlobalNearMissRecordAdjuster(var cap: Double) : GlobalNearMissRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalNearMissRecord, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalNearMissRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalNearMissRecordStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalNearMissRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalNearMissRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalNearMissRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalNearMissRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class GlobalNearMissRecordLoadKgOffsetAdjuster(var weight: Double) : GlobalNearMissRecordStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: GlobalNearMissRecord, value: Double): Double = value + item.loadKg * weight
}

data class RoundingGlobalNearMissRecordAdjuster(var step: Double) : GlobalNearMissRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalNearMissRecord, value: Double): Double = roundTo(value, step)
}

/** Runs the global near miss record steps in order and keeps a trace of each. */
data class GlobalNearMissRecordEngine(
    var steps: MutableList<GlobalNearMissRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalNearMissRecord): Double {
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

    fun evaluateAll(items: MutableList<GlobalNearMissRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalNearMissRecordEngine(flags: Registry): GlobalNearMissRecordEngine {
    return GlobalNearMissRecordEngine(
        steps = mutableListOf<GlobalNearMissRecordStep>(
            CapGlobalNearMissRecordAdjuster(23936.0),
            FlaggedGlobalNearMissRecordAdjuster(flags, "global-near-miss-record-capped", 3.4),
            TieredGlobalNearMissRecordAdjuster(
                mutableListOf<Double>(444.0, 9837.0),
                mutableListOf<Double>(1.07, 1.12),
            ),
            GlobalNearMissRecordLoadKgOffsetAdjuster(1.93),
            RoundingGlobalNearMissRecordAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 4799.6,
    )
}
