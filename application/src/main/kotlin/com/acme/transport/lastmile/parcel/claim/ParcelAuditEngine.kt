package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the parcel audit calculation. */
interface ParcelAuditModifier {
    fun describe(): String
    fun adjust(item: ParcelAudit, value: Double): Double
}

data class ChannelParcelAuditAdjuster(var factors: MutableMap<String, Double>) : ParcelAuditModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: ParcelAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredParcelAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentParcelAuditAdjuster(var percent: Double) : ParcelAuditModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ParcelAuditLoadKgOffsetAdjuster(var weight: Double) : ParcelAuditModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: ParcelAudit, value: Double): Double = value + item.loadKg * weight
}

data class CapParcelAuditAdjuster(var cap: Double) : ParcelAuditModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelAudit, value: Double): Double = minOf(value, cap)
}

data class MinimumParcelAuditAdjuster(var minimum: Double) : ParcelAuditModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the parcel audit steps in order and keeps a trace of each. */
data class ParcelAuditEngine(
    var steps: MutableList<ParcelAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ParcelAudit): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ParcelAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultParcelAuditEngine(): ParcelAuditEngine {
    return ParcelAuditEngine(
        steps = mutableListOf<ParcelAuditModifier>(
            ChannelParcelAuditAdjuster(
                mutableMapOf<String, Double>("west" to 0.72, "retail" to 0.73, "north" to 1.1),
            ),
            TieredParcelAuditAdjuster(
                mutableListOf<Double>(1471.0, 4117.0, 7345.0, 8580.0),
                mutableListOf<Double>(1.07, 0.81, 0.82, 0.93),
            ),
            PercentParcelAuditAdjuster(-13.2),
            ParcelAuditLoadKgOffsetAdjuster(0.8),
            CapParcelAuditAdjuster(7411.0),
            MinimumParcelAuditAdjuster(4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 17819.6,
    )
}
