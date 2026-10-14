package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy lease return entry calculation. */
interface LegacyLeaseReturnEntryFactor {
    fun describe(): String
    fun adjust(item: LegacyLeaseReturnEntry, value: Double): Double
}

data class RoundingLegacyLeaseReturnEntryAdjuster(var step: Double) : LegacyLeaseReturnEntryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyLeaseReturnEntry, value: Double): Double = roundTo(value, step)
}

data class LegacyLeaseReturnEntryCreatedAtOffsetAdjuster(var weight: Double) : LegacyLeaseReturnEntryFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: LegacyLeaseReturnEntry, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class CapLegacyLeaseReturnEntryAdjuster(var cap: Double) : LegacyLeaseReturnEntryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyLeaseReturnEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy lease return entry steps in order and keeps a trace of each. */
data class LegacyLeaseReturnEntryEngine(
    var steps: MutableList<LegacyLeaseReturnEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyLeaseReturnEntry): Double {
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

    fun evaluateAll(items: MutableList<LegacyLeaseReturnEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyLeaseReturnEntryEngine(): LegacyLeaseReturnEntryEngine {
    return LegacyLeaseReturnEntryEngine(
        steps = mutableListOf<LegacyLeaseReturnEntryFactor>(
            RoundingLegacyLeaseReturnEntryAdjuster(5.0),
            LegacyLeaseReturnEntryCreatedAtOffsetAdjuster(0.52),
            CapLegacyLeaseReturnEntryAdjuster(22440.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 65194.8,
    )
}
