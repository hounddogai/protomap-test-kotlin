package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the clearance exception calculation. */
interface ClearanceExceptionStep {
    fun describe(): String
    fun adjust(item: ClearanceException, value: Double): Double
}

data class FlaggedClearanceExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceExceptionStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ClearanceExceptionExpiresAtOffsetAdjuster(var weight: Double) : ClearanceExceptionStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: ClearanceException, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class PercentClearanceExceptionAdjuster(var percent: Double) : ClearanceExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceException, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the clearance exception steps in order and keeps a trace of each. */
data class ClearanceExceptionEngine(
    var steps: MutableList<ClearanceExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ClearanceException): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultClearanceExceptionEngine(flags: Registry): ClearanceExceptionEngine {
    return ClearanceExceptionEngine(
        steps = mutableListOf<ClearanceExceptionStep>(
            FlaggedClearanceExceptionAdjuster(flags, "clearance-exception-strict", 6.8),
            ClearanceExceptionExpiresAtOffsetAdjuster(1.35),
            PercentClearanceExceptionAdjuster(4.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 97843.7,
    )
}
