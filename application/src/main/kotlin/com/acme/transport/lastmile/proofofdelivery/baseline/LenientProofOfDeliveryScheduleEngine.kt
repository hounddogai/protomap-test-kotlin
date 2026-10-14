package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient proof of delivery schedule calculation. */
interface LenientProofOfDeliveryScheduleStep {
    fun describe(): String
    fun adjust(item: LenientProofOfDeliverySchedule, value: Double): Double
}

data class RoundingLenientProofOfDeliveryScheduleAdjuster(
    var step: Double,
) : LenientProofOfDeliveryScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientProofOfDeliverySchedule, value: Double): Double = roundTo(value, step)
}

data class LenientProofOfDeliveryScheduleDiscountOffsetAdjuster(
    var weight: Double,
) : LenientProofOfDeliveryScheduleStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: LenientProofOfDeliverySchedule, value: Double): Double = value + item.discount * weight
}

data class CapLenientProofOfDeliveryScheduleAdjuster(var cap: Double) : LenientProofOfDeliveryScheduleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientProofOfDeliverySchedule, value: Double): Double = minOf(value, cap)
}

data class DepotLenientProofOfDeliveryScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientProofOfDeliveryScheduleStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: LenientProofOfDeliverySchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientProofOfDeliveryScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientProofOfDeliveryScheduleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientProofOfDeliverySchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient proof of delivery schedule steps in order and keeps a trace of each. */
data class LenientProofOfDeliveryScheduleEngine(
    var steps: MutableList<LenientProofOfDeliveryScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientProofOfDeliverySchedule): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientProofOfDeliverySchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientProofOfDeliveryScheduleEngine(flags: Registry): LenientProofOfDeliveryScheduleEngine {
    return LenientProofOfDeliveryScheduleEngine(
        steps = mutableListOf<LenientProofOfDeliveryScheduleStep>(
            RoundingLenientProofOfDeliveryScheduleAdjuster(5.0),
            LenientProofOfDeliveryScheduleDiscountOffsetAdjuster(0.6),
            CapLenientProofOfDeliveryScheduleAdjuster(24802.0),
            DepotLenientProofOfDeliveryScheduleAdjuster(
                mutableMapOf<String, Double>("south" to 0.71, "retail" to 0.97, "west" to 1.24),
            ),
            FlaggedLenientProofOfDeliveryScheduleAdjuster(
                flags,
                "lenient-proof-of-delivery-schedule-global",
                5.3,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 66782.7,
    )
}
