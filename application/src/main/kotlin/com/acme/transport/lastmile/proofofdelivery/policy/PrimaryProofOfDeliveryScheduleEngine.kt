package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary proof of delivery schedule calculation. */
interface PrimaryProofOfDeliveryScheduleModifier {
    fun describe(): String
    fun adjust(item: PrimaryProofOfDeliverySchedule, value: Double): Double
}

data class PrimaryProofOfDeliveryScheduleFuelLitersOffsetAdjuster(
    var weight: Double,
) : PrimaryProofOfDeliveryScheduleModifier {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: PrimaryProofOfDeliverySchedule, value: Double): Double {
        return value + item.fuelLiters * weight
    }
}

data class MinimumPrimaryProofOfDeliveryScheduleAdjuster(
    var minimum: Double,
) : PrimaryProofOfDeliveryScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryProofOfDeliverySchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryProofOfDeliveryScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryProofOfDeliveryScheduleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryProofOfDeliverySchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPrimaryProofOfDeliveryScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryProofOfDeliveryScheduleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryProofOfDeliverySchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionPrimaryProofOfDeliveryScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryProofOfDeliveryScheduleModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: PrimaryProofOfDeliverySchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPrimaryProofOfDeliveryScheduleAdjuster(
    var step: Double,
) : PrimaryProofOfDeliveryScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryProofOfDeliverySchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the primary proof of delivery schedule steps in order and keeps a trace of each. */
data class PrimaryProofOfDeliveryScheduleEngine(
    var steps: MutableList<PrimaryProofOfDeliveryScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryProofOfDeliverySchedule): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryProofOfDeliverySchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryProofOfDeliveryScheduleEngine(flags: Registry): PrimaryProofOfDeliveryScheduleEngine {
    return PrimaryProofOfDeliveryScheduleEngine(
        steps = mutableListOf<PrimaryProofOfDeliveryScheduleModifier>(
            PrimaryProofOfDeliveryScheduleFuelLitersOffsetAdjuster(0.77),
            MinimumPrimaryProofOfDeliveryScheduleAdjuster(8.5),
            TieredPrimaryProofOfDeliveryScheduleAdjuster(
                mutableListOf<Double>(268.0, 5822.0, 7779.0, 9812.0),
                mutableListOf<Double>(1.03, 1.0, 1.11, 0.89),
            ),
            FlaggedPrimaryProofOfDeliveryScheduleAdjuster(
                flags,
                "primary-proof-of-delivery-schedule-manual",
                -0.6,
            ),
            RegionPrimaryProofOfDeliveryScheduleAdjuster(
                mutableMapOf<String, Double>("online" to 1.18, "north" to 0.77, "retail" to 0.76),
            ),
            RoundingPrimaryProofOfDeliveryScheduleAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 63913.6,
    )
}
