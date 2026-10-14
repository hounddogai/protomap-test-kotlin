package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.numbers.roundTo

/** One step of the violation notice calculation. */
interface ViolationNoticeFactor {
    fun describe(): String
    fun adjust(item: ViolationNotice, value: Double): Double
}

data class CapViolationNoticeAdjuster(var cap: Double) : ViolationNoticeFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationNotice, value: Double): Double = minOf(value, cap)
}

data class RoundingViolationNoticeAdjuster(var step: Double) : ViolationNoticeFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationNotice, value: Double): Double = roundTo(value, step)
}

data class ViolationNoticeMaximumOffsetAdjuster(var weight: Double) : ViolationNoticeFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ViolationNotice, value: Double): Double = value + item.maximum * weight
}

data class MinimumViolationNoticeAdjuster(var minimum: Double) : ViolationNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitViolationNoticeAdjuster(var factors: MutableMap<String, Double>) : ViolationNoticeFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: ViolationNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the violation notice steps in order and keeps a trace of each. */
data class ViolationNoticeEngine(
    var steps: MutableList<ViolationNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ViolationNotice): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationNoticeFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ViolationNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultViolationNoticeEngine(): ViolationNoticeEngine {
    return ViolationNoticeEngine(
        steps = mutableListOf<ViolationNoticeFactor>(
            CapViolationNoticeAdjuster(28146.0),
            RoundingViolationNoticeAdjuster(0.05),
            ViolationNoticeMaximumOffsetAdjuster(1.49),
            MinimumViolationNoticeAdjuster(11.8),
            UnitViolationNoticeAdjuster(
                mutableMapOf<String, Double>("central" to 1.11, "north" to 1.27, "west" to 1.3),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 35348.8,
    )
}
