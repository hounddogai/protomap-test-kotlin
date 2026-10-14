package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched locker claim calculation. */
interface BatchedLockerClaimAdjuster {
    fun describe(): String
    fun adjust(item: BatchedLockerClaim, value: Double): Double
}

data class RoundingBatchedLockerClaimAdjuster(var step: Double) : BatchedLockerClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedLockerClaim, value: Double): Double = roundTo(value, step)
}

data class FlaggedBatchedLockerClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedLockerClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedLockerClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedLockerClaimAdjuster(var minimum: Double) : BatchedLockerClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedLockerClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched locker claim steps in order and keeps a trace of each. */
data class BatchedLockerClaimEngine(
    var steps: MutableList<BatchedLockerClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedLockerClaim): Double {
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

    fun evaluateAll(items: MutableList<BatchedLockerClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedLockerClaimEngine(flags: Registry): BatchedLockerClaimEngine {
    return BatchedLockerClaimEngine(
        steps = mutableListOf<BatchedLockerClaimAdjuster>(
            RoundingBatchedLockerClaimAdjuster(5.0),
            FlaggedBatchedLockerClaimAdjuster(flags, "batched-locker-claim-default", -1.8),
            MinimumBatchedLockerClaimAdjuster(18.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 79030.0,
    )
}
