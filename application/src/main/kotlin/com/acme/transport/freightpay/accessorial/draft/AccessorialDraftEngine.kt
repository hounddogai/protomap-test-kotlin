package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the accessorial draft calculation. */
interface AccessorialDraftAdjuster {
    fun describe(): String
    fun adjust(item: AccessorialDraft, value: Double): Double
}

data class NameAccessorialDraftAdjuster(var factors: MutableMap<String, Double>) : AccessorialDraftAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: AccessorialDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAccessorialDraftAdjuster(var cap: Double) : AccessorialDraftAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialDraft, value: Double): Double = minOf(value, cap)
}

data class MinimumAccessorialDraftAdjuster(var minimum: Double) : AccessorialDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AccessorialDraftCeilingOffsetAdjuster(var weight: Double) : AccessorialDraftAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: AccessorialDraft, value: Double): Double = value + item.ceiling * weight
}

/** Runs the accessorial draft steps in order and keeps a trace of each. */
data class AccessorialDraftEngine(
    var steps: MutableList<AccessorialDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialDraft): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialDraftEngine(): AccessorialDraftEngine {
    return AccessorialDraftEngine(
        steps = mutableListOf<AccessorialDraftAdjuster>(
            NameAccessorialDraftAdjuster(mutableMapOf<String, Double>("central" to 0.98, "online" to 0.84)),
            CapAccessorialDraftAdjuster(23244.0),
            MinimumAccessorialDraftAdjuster(4.1),
            AccessorialDraftCeilingOffsetAdjuster(0.58),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 86962.9,
    )
}
