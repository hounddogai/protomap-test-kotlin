package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the near miss record calculation. */
interface NearMissRecordAdjuster {
    fun describe(): String
    fun adjust(item: NearMissRecord, value: Double): Double
}

data class RoundingNearMissRecordAdjuster(var step: Double) : NearMissRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissRecord, value: Double): Double = roundTo(value, step)
}

data class TieredNearMissRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapNearMissRecordAdjuster(var cap: Double) : NearMissRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissRecord, value: Double): Double = minOf(value, cap)
}

data class FlaggedNearMissRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumNearMissRecordAdjuster(var minimum: Double) : NearMissRecordAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the near miss record steps in order and keeps a trace of each. */
data class NearMissRecordEngine(
    var steps: MutableList<NearMissRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: NearMissRecord): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultNearMissRecordEngine(flags: Registry): NearMissRecordEngine {
    return NearMissRecordEngine(
        steps = mutableListOf<NearMissRecordAdjuster>(
            RoundingNearMissRecordAdjuster(0.25),
            TieredNearMissRecordAdjuster(
                mutableListOf<Double>(3275.0, 4181.0, 8450.0),
                mutableListOf<Double>(1.06, 1.08, 1.16),
            ),
            CapNearMissRecordAdjuster(35191.0),
            FlaggedNearMissRecordAdjuster(flags, "near-miss-record-capped", 2.3),
            MinimumNearMissRecordAdjuster(11.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 10783.1,
    )
}
