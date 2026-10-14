package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary zone quota calculation. */
interface PrimaryZoneQuotaStep {
    fun describe(): String
    fun adjust(item: PrimaryZoneQuota, value: Double): Double
}

data class RoundingPrimaryZoneQuotaAdjuster(var step: Double) : PrimaryZoneQuotaStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryZoneQuota, value: Double): Double = roundTo(value, step)
}

data class PrimaryZoneQuotaLimitOffsetAdjuster(var weight: Double) : PrimaryZoneQuotaStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: PrimaryZoneQuota, value: Double): Double = value + item.limit.toDouble() * weight
}

data class CapPrimaryZoneQuotaAdjuster(var cap: Double) : PrimaryZoneQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryZoneQuota, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryZoneQuotaAdjuster(var minimum: Double) : PrimaryZoneQuotaStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryZoneQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary zone quota steps in order and keeps a trace of each. */
data class PrimaryZoneQuotaEngine(
    var steps: MutableList<PrimaryZoneQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryZoneQuota): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryZoneQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryZoneQuotaEngine(): PrimaryZoneQuotaEngine {
    return PrimaryZoneQuotaEngine(
        steps = mutableListOf<PrimaryZoneQuotaStep>(
            RoundingPrimaryZoneQuotaAdjuster(5.0),
            PrimaryZoneQuotaLimitOffsetAdjuster(0.16),
            CapPrimaryZoneQuotaAdjuster(9266.0),
            MinimumPrimaryZoneQuotaAdjuster(46.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 71436.1,
    )
}
