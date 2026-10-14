package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped node agreement calculation. */
interface CappedNodeAgreementAdjuster {
    fun describe(): String
    fun adjust(item: CappedNodeAgreement, value: Double): Double
}

data class TieredCappedNodeAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedNodeAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedNodeAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedNodeAgreementAdjuster(var cap: Double) : CappedNodeAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedNodeAgreement, value: Double): Double = minOf(value, cap)
}

data class TitleCappedNodeAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedNodeAgreementAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedNodeAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCappedNodeAgreementAdjuster(var minimum: Double) : CappedNodeAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedNodeAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedNodeAgreementAdjuster(var step: Double) : CappedNodeAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedNodeAgreement, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedNodeAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedNodeAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedNodeAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped node agreement steps in order and keeps a trace of each. */
data class CappedNodeAgreementEngine(
    var steps: MutableList<CappedNodeAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedNodeAgreement): Double {
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

    fun evaluateAll(items: MutableList<CappedNodeAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedNodeAgreementEngine(flags: Registry): CappedNodeAgreementEngine {
    return CappedNodeAgreementEngine(
        steps = mutableListOf<CappedNodeAgreementAdjuster>(
            TieredCappedNodeAgreementAdjuster(
                mutableListOf<Double>(1712.0, 4083.0, 5033.0, 9532.0),
                mutableListOf<Double>(1.03, 1.02, 1.13, 0.82),
            ),
            CapCappedNodeAgreementAdjuster(15358.0),
            TitleCappedNodeAgreementAdjuster(mutableMapOf<String, Double>("east" to 0.77)),
            MinimumCappedNodeAgreementAdjuster(30.2),
            RoundingCappedNodeAgreementAdjuster(0.1),
            FlaggedCappedNodeAgreementAdjuster(flags, "capped-node-agreement-scheduled", 3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 45444.9,
    )
}
