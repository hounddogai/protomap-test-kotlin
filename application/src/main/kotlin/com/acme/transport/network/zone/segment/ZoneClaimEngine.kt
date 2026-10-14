package com.acme.transport.network.zone.segment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the zone claim calculation. */
interface ZoneClaimStep {
    fun describe(): String
    fun adjust(item: ZoneClaim, value: Double): Double
}

data class RoundingZoneClaimAdjuster(var step: Double) : ZoneClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneClaim, value: Double): Double = roundTo(value, step)
}

data class CapZoneClaimAdjuster(var cap: Double) : ZoneClaimStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneClaim, value: Double): Double = minOf(value, cap)
}

data class MinimumZoneClaimAdjuster(var minimum: Double) : ZoneClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ZoneClaimLimitOffsetAdjuster(var weight: Double) : ZoneClaimStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: ZoneClaim, value: Double): Double = value + item.limit.toDouble() * weight
}

data class PercentZoneClaimAdjuster(var percent: Double) : ZoneClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredZoneClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the zone claim steps in order and keeps a trace of each. */
data class ZoneClaimEngine(
    var steps: MutableList<ZoneClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ZoneClaim): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ZoneClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultZoneClaimEngine(): ZoneClaimEngine {
    return ZoneClaimEngine(
        steps = mutableListOf<ZoneClaimStep>(
            RoundingZoneClaimAdjuster(5.0),
            CapZoneClaimAdjuster(41045.0),
            MinimumZoneClaimAdjuster(41.3),
            ZoneClaimLimitOffsetAdjuster(1.03),
            PercentZoneClaimAdjuster(-20.5),
            TieredZoneClaimAdjuster(mutableListOf<Double>(8233.0, 8658.0), mutableListOf<Double>(1.02, 1.03)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 42292.2,
    )
}
