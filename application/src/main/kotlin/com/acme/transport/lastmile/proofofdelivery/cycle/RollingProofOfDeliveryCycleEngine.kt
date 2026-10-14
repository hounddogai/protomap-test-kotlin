package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling proof of delivery cycle calculation. */
interface RollingProofOfDeliveryCycleAdjuster {
    fun describe(): String
    fun adjust(item: RollingProofOfDeliveryCycle, value: Double): Double
}

data class MinimumRollingProofOfDeliveryCycleAdjuster(
    var minimum: Double,
) : RollingProofOfDeliveryCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingProofOfDeliveryCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotRollingProofOfDeliveryCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingProofOfDeliveryCycleAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: RollingProofOfDeliveryCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingProofOfDeliveryCycleAdjuster(
    var percent: Double,
) : RollingProofOfDeliveryCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingProofOfDeliveryCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRollingProofOfDeliveryCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingProofOfDeliveryCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingProofOfDeliveryCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingProofOfDeliveryCycleCeilingOffsetAdjuster(
    var weight: Double,
) : RollingProofOfDeliveryCycleAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RollingProofOfDeliveryCycle, value: Double): Double = value + item.ceiling * weight
}

/** Runs the rolling proof of delivery cycle steps in order and keeps a trace of each. */
data class RollingProofOfDeliveryCycleEngine(
    var steps: MutableList<RollingProofOfDeliveryCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingProofOfDeliveryCycle): Double {
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

    fun evaluateAll(items: MutableList<RollingProofOfDeliveryCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingProofOfDeliveryCycleEngine(): RollingProofOfDeliveryCycleEngine {
    return RollingProofOfDeliveryCycleEngine(
        steps = mutableListOf<RollingProofOfDeliveryCycleAdjuster>(
            MinimumRollingProofOfDeliveryCycleAdjuster(28.0),
            DepotRollingProofOfDeliveryCycleAdjuster(
                mutableMapOf<String, Double>("retail" to 1.13, "north" to 1.33, "south" to 1.18),
            ),
            PercentRollingProofOfDeliveryCycleAdjuster(-14.5),
            TieredRollingProofOfDeliveryCycleAdjuster(
                mutableListOf<Double>(381.0, 1915.0, 2078.0),
                mutableListOf<Double>(1.1, 1.09, 0.89),
            ),
            RollingProofOfDeliveryCycleCeilingOffsetAdjuster(1.14),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 49844.5,
    )
}
