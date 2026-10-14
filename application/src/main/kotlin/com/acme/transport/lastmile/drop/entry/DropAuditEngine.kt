package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the drop audit calculation. */
interface DropAuditAdjuster {
    fun describe(): String
    fun adjust(item: DropAudit, value: Double): Double
}

data class DropAuditRatioOffsetAdjuster(var weight: Double) : DropAuditAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: DropAudit, value: Double): Double = value + item.ratio * weight
}

data class CapDropAuditAdjuster(var cap: Double) : DropAuditAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropAudit, value: Double): Double = minOf(value, cap)
}

data class TieredDropAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDropAuditAdjuster(var step: Double) : DropAuditAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropAudit, value: Double): Double = roundTo(value, step)
}

/** Runs the drop audit steps in order and keeps a trace of each. */
data class DropAuditEngine(
    var steps: MutableList<DropAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DropAudit): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DropAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDropAuditEngine(): DropAuditEngine {
    return DropAuditEngine(
        steps = mutableListOf<DropAuditAdjuster>(
            DropAuditRatioOffsetAdjuster(1.76),
            CapDropAuditAdjuster(41649.0),
            TieredDropAuditAdjuster(
                mutableListOf<Double>(63.0, 3863.0, 8022.0),
                mutableListOf<Double>(0.97, 0.82, 1.01),
            ),
            RoundingDropAuditAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 26139.0,
    )
}
