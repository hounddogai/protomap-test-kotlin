package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled dock door claim calculation. */
interface ScheduledDockDoorClaimModifier {
    fun describe(): String
    fun adjust(item: ScheduledDockDoorClaim, value: Double): Double
}

data class TieredScheduledDockDoorClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDockDoorClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDockDoorClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ScheduledDockDoorClaimPriorityOffsetAdjuster(var weight: Double) : ScheduledDockDoorClaimModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: ScheduledDockDoorClaim, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class CategoryScheduledDockDoorClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDockDoorClaimModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: ScheduledDockDoorClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledDockDoorClaimAdjuster(var step: Double) : ScheduledDockDoorClaimModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledDockDoorClaim, value: Double): Double = roundTo(value, step)
}

data class CapScheduledDockDoorClaimAdjuster(var cap: Double) : ScheduledDockDoorClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDockDoorClaim, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledDockDoorClaimAdjuster(var percent: Double) : ScheduledDockDoorClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledDockDoorClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled dock door claim steps in order and keeps a trace of each. */
data class ScheduledDockDoorClaimEngine(
    var steps: MutableList<ScheduledDockDoorClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledDockDoorClaim): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDockDoorClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDockDoorClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledDockDoorClaimEngine(): ScheduledDockDoorClaimEngine {
    return ScheduledDockDoorClaimEngine(
        steps = mutableListOf<ScheduledDockDoorClaimModifier>(
            TieredScheduledDockDoorClaimAdjuster(
                mutableListOf<Double>(6768.0, 8206.0),
                mutableListOf<Double>(1.14, 1.03),
            ),
            ScheduledDockDoorClaimPriorityOffsetAdjuster(1.18),
            CategoryScheduledDockDoorClaimAdjuster(mutableMapOf<String, Double>("south" to 0.87)),
            RoundingScheduledDockDoorClaimAdjuster(0.5),
            CapScheduledDockDoorClaimAdjuster(1767.0),
            PercentScheduledDockDoorClaimAdjuster(4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 73789.4,
    )
}
