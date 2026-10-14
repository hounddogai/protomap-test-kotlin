package com.acme.transport.safety.violation.entry

/** One step of the rolling violation claim calculation. */
interface RollingViolationClaimAdjuster {
    fun describe(): String
    fun adjust(item: RollingViolationClaim, value: Double): Double
}

data class RollingViolationClaimCeilingOffsetAdjuster(var weight: Double) : RollingViolationClaimAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RollingViolationClaim, value: Double): Double = value + item.ceiling * weight
}

data class MinimumRollingViolationClaimAdjuster(var minimum: Double) : RollingViolationClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingViolationClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRollingViolationClaimAdjuster(var cap: Double) : RollingViolationClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingViolationClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling violation claim steps in order and keeps a trace of each. */
data class RollingViolationClaimEngine(
    var steps: MutableList<RollingViolationClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingViolationClaim): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingViolationClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingViolationClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingViolationClaimEngine(): RollingViolationClaimEngine {
    return RollingViolationClaimEngine(
        steps = mutableListOf<RollingViolationClaimAdjuster>(
            RollingViolationClaimCeilingOffsetAdjuster(0.21),
            MinimumRollingViolationClaimAdjuster(36.1),
            CapRollingViolationClaimAdjuster(38143.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 48146.4,
    )
}
