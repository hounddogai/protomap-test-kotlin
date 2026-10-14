package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global node agreement calculation. */
interface GlobalNodeAgreementFactor {
    fun describe(): String
    fun adjust(item: GlobalNodeAgreement, value: Double): Double
}

data class TieredGlobalNodeAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalNodeAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalNodeAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalNodeAgreementAdjuster(var cap: Double) : GlobalNodeAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalNodeAgreement, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalNodeAgreementAdjuster(var minimum: Double) : GlobalNodeAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalNodeAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalNodeAgreementAdjuster(var percent: Double) : GlobalNodeAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalNodeAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentGlobalNodeAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalNodeAgreementFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: GlobalNodeAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalNodeAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalNodeAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalNodeAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global node agreement steps in order and keeps a trace of each. */
data class GlobalNodeAgreementEngine(
    var steps: MutableList<GlobalNodeAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalNodeAgreement): Double {
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

    fun evaluateAll(items: MutableList<GlobalNodeAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalNodeAgreementEngine(flags: Registry): GlobalNodeAgreementEngine {
    return GlobalNodeAgreementEngine(
        steps = mutableListOf<GlobalNodeAgreementFactor>(
            TieredGlobalNodeAgreementAdjuster(
                mutableListOf<Double>(6449.0, 7230.0, 9970.0),
                mutableListOf<Double>(1.05, 1.11, 0.93),
            ),
            CapGlobalNodeAgreementAdjuster(653.0),
            MinimumGlobalNodeAgreementAdjuster(48.3),
            PercentGlobalNodeAgreementAdjuster(-19.8),
            SegmentGlobalNodeAgreementAdjuster(
                mutableMapOf<String, Double>("north" to 1.11, "south" to 0.77, "central" to 1.03),
            ),
            FlaggedGlobalNodeAgreementAdjuster(flags, "global-node-agreement-primary", 8.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 95478.9,
    )
}
