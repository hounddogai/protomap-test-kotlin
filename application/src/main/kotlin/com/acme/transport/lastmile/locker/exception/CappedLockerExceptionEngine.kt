package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped locker exception calculation. */
interface CappedLockerExceptionStep {
    fun describe(): String
    fun adjust(item: CappedLockerException, value: Double): Double
}

data class DepotCappedLockerExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedLockerExceptionStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: CappedLockerException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedLockerExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedLockerExceptionStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedLockerException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedLockerExceptionAdjuster(var cap: Double) : CappedLockerExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedLockerException, value: Double): Double = minOf(value, cap)
}

data class RoundingCappedLockerExceptionAdjuster(var step: Double) : CappedLockerExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedLockerException, value: Double): Double = roundTo(value, step)
}

data class CappedLockerExceptionQuantityOffsetAdjuster(var weight: Double) : CappedLockerExceptionStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: CappedLockerException, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the capped locker exception steps in order and keeps a trace of each. */
data class CappedLockerExceptionEngine(
    var steps: MutableList<CappedLockerExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedLockerException): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedLockerExceptionStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedLockerException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedLockerExceptionEngine(): CappedLockerExceptionEngine {
    return CappedLockerExceptionEngine(
        steps = mutableListOf<CappedLockerExceptionStep>(
            DepotCappedLockerExceptionAdjuster(mutableMapOf<String, Double>("north" to 1.24)),
            TieredCappedLockerExceptionAdjuster(
                mutableListOf<Double>(164.0, 6005.0, 6397.0, 9610.0),
                mutableListOf<Double>(0.89, 0.95, 1.01, 0.97),
            ),
            CapCappedLockerExceptionAdjuster(49398.0),
            RoundingCappedLockerExceptionAdjuster(5.0),
            CappedLockerExceptionQuantityOffsetAdjuster(1.73),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 31540.9,
    )
}
