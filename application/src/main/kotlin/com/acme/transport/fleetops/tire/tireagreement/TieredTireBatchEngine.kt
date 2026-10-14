package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered tire batch calculation. */
interface TieredTireBatchStep {
    fun describe(): String
    fun adjust(item: TieredTireBatch, value: Double): Double
}

data class TieredTieredTireBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredTireBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredTireBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTieredTireBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredTireBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredTireBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredTireBatchAdjuster(var cap: Double) : TieredTireBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredTireBatch, value: Double): Double = minOf(value, cap)
}

data class MinimumTieredTireBatchAdjuster(var minimum: Double) : TieredTireBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredTireBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTireBatchEtaMinutesOffsetAdjuster(var weight: Double) : TieredTireBatchStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: TieredTireBatch, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

/** Runs the tiered tire batch steps in order and keeps a trace of each. */
data class TieredTireBatchEngine(
    var steps: MutableList<TieredTireBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredTireBatch): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredTireBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredTireBatchEngine(flags: Registry): TieredTireBatchEngine {
    return TieredTireBatchEngine(
        steps = mutableListOf<TieredTireBatchStep>(
            TieredTieredTireBatchAdjuster(
                mutableListOf<Double>(768.0, 1419.0, 5207.0),
                mutableListOf<Double>(1.17, 1.18, 1.18),
            ),
            FlaggedTieredTireBatchAdjuster(flags, "tiered-tire-batch-seasonal", -0.4),
            CapTieredTireBatchAdjuster(8013.0),
            MinimumTieredTireBatchAdjuster(35.0),
            TieredTireBatchEtaMinutesOffsetAdjuster(1.62),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 37548.2,
    )
}
