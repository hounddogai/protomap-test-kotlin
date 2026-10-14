package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lease return entry calculation. */
interface LeaseReturnEntryStep {
    fun describe(): String
    fun adjust(item: LeaseReturnEntry, value: Double): Double
}

data class RoundingLeaseReturnEntryAdjuster(var step: Double) : LeaseReturnEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnEntry, value: Double): Double = roundTo(value, step)
}

data class TieredLeaseReturnEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLeaseReturnEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LeaseReturnEntryAttemptsOffsetAdjuster(var weight: Double) : LeaseReturnEntryStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: LeaseReturnEntry, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the lease return entry steps in order and keeps a trace of each. */
data class LeaseReturnEntryEngine(
    var steps: MutableList<LeaseReturnEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LeaseReturnEntry): Double {
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

    fun evaluateAll(items: MutableList<LeaseReturnEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLeaseReturnEntryEngine(flags: Registry): LeaseReturnEntryEngine {
    return LeaseReturnEntryEngine(
        steps = mutableListOf<LeaseReturnEntryStep>(
            RoundingLeaseReturnEntryAdjuster(0.1),
            TieredLeaseReturnEntryAdjuster(
                mutableListOf<Double>(2633.0, 9595.0),
                mutableListOf<Double>(1.11, 0.87),
            ),
            FlaggedLeaseReturnEntryAdjuster(flags, "lease-return-entry-strict", -4.0),
            LeaseReturnEntryAttemptsOffsetAdjuster(1.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 84875.4,
    )
}
