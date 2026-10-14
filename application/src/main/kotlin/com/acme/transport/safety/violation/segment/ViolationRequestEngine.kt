package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the violation request calculation. */
interface ViolationRequestFactor {
    fun describe(): String
    fun adjust(item: ViolationRequest, value: Double): Double
}

data class CategoryViolationRequestAdjuster(var factors: MutableMap<String, Double>) : ViolationRequestFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: ViolationRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumViolationRequestAdjuster(var minimum: Double) : ViolationRequestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedViolationRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the violation request steps in order and keeps a trace of each. */
data class ViolationRequestEngine(
    var steps: MutableList<ViolationRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ViolationRequest): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultViolationRequestEngine(flags: Registry): ViolationRequestEngine {
    return ViolationRequestEngine(
        steps = mutableListOf<ViolationRequestFactor>(
            CategoryViolationRequestAdjuster(mutableMapOf<String, Double>("west" to 1.34)),
            MinimumViolationRequestAdjuster(12.6),
            FlaggedViolationRequestAdjuster(flags, "violation-request-lenient", 5.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 60885.0,
    )
}
