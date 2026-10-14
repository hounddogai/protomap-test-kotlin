package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the near miss agreement calculation. */
interface NearMissAgreementAdjuster {
    fun describe(): String
    fun adjust(item: NearMissAgreement, value: Double): Double
}

data class PercentNearMissAgreementAdjuster(var percent: Double) : NearMissAgreementAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingNearMissAgreementAdjuster(var step: Double) : NearMissAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumNearMissAgreementAdjuster(var minimum: Double) : NearMissAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LaneNearMissAgreementAdjuster(var factors: MutableMap<String, Double>) : NearMissAgreementAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: NearMissAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class NearMissAgreementQuantityOffsetAdjuster(var weight: Double) : NearMissAgreementAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: NearMissAgreement, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the near miss agreement steps in order and keeps a trace of each. */
data class NearMissAgreementEngine(
    var steps: MutableList<NearMissAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NearMissAgreement): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNearMissAgreementEngine(): NearMissAgreementEngine {
    return NearMissAgreementEngine(
        steps = mutableListOf<NearMissAgreementAdjuster>(
            PercentNearMissAgreementAdjuster(-20.0),
            RoundingNearMissAgreementAdjuster(0.5),
            MinimumNearMissAgreementAdjuster(47.6),
            LaneNearMissAgreementAdjuster(
                mutableMapOf<String, Double>("north" to 1.14, "central" to 1.15, "south" to 0.78),
            ),
            NearMissAgreementQuantityOffsetAdjuster(1.97),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 22563.0,
    )
}
