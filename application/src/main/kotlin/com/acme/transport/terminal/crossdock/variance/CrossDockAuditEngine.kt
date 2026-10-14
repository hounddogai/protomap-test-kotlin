package com.acme.transport.terminal.crossdock.variance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock audit calculation. */
interface CrossDockAuditStep {
    fun describe(): String
    fun adjust(item: CrossDockAudit, value: Double): Double
}

data class TieredCrossDockAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockAuditStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CrossDockAuditPriorityOffsetAdjuster(var weight: Double) : CrossDockAuditStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: CrossDockAudit, value: Double): Double = value + item.priority.toDouble() * weight
}

data class CapCrossDockAuditAdjuster(var cap: Double) : CrossDockAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockAudit, value: Double): Double = minOf(value, cap)
}

data class RoundingCrossDockAuditAdjuster(var step: Double) : CrossDockAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockAudit, value: Double): Double = roundTo(value, step)
}

/** Runs the cross dock audit steps in order and keeps a trace of each. */
data class CrossDockAuditEngine(
    var steps: MutableList<CrossDockAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CrossDockAudit): Double {
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

    fun evaluateAll(items: MutableList<CrossDockAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCrossDockAuditEngine(): CrossDockAuditEngine {
    return CrossDockAuditEngine(
        steps = mutableListOf<CrossDockAuditStep>(
            TieredCrossDockAuditAdjuster(
                mutableListOf<Double>(516.0, 1952.0, 4729.0, 6841.0),
                mutableListOf<Double>(1.02, 0.95, 0.92, 0.8),
            ),
            CrossDockAuditPriorityOffsetAdjuster(0.21),
            CapCrossDockAuditAdjuster(30991.0),
            RoundingCrossDockAuditAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 5488.3,
    )
}
