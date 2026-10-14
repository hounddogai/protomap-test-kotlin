package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled proof of delivery preference calculation. */
interface ScheduledProofOfDeliveryPreferenceStep {
    fun describe(): String
    fun adjust(item: ScheduledProofOfDeliveryPreference, value: Double): Double
}

data class TieredScheduledProofOfDeliveryPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledProofOfDeliveryPreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledProofOfDeliveryPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledProofOfDeliveryPreferenceAdjuster(
    var percent: Double,
) : ScheduledProofOfDeliveryPreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledProofOfDeliveryPreference, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class CapScheduledProofOfDeliveryPreferenceAdjuster(
    var cap: Double,
) : ScheduledProofOfDeliveryPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledProofOfDeliveryPreference, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledProofOfDeliveryPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledProofOfDeliveryPreferenceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledProofOfDeliveryPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledProofOfDeliveryPreferenceBatchSizeOffsetAdjuster(
    var weight: Double,
) : ScheduledProofOfDeliveryPreferenceStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: ScheduledProofOfDeliveryPreference, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class SegmentScheduledProofOfDeliveryPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledProofOfDeliveryPreferenceStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: ScheduledProofOfDeliveryPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled proof of delivery preference steps in order and keeps a trace of each. */
data class ScheduledProofOfDeliveryPreferenceEngine(
    var steps: MutableList<ScheduledProofOfDeliveryPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledProofOfDeliveryPreference): Double {
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

    fun evaluateAll(items: MutableList<ScheduledProofOfDeliveryPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledProofOfDeliveryPreferenceEngine(flags: Registry): ScheduledProofOfDeliveryPreferenceEngine {
    return ScheduledProofOfDeliveryPreferenceEngine(
        steps = mutableListOf<ScheduledProofOfDeliveryPreferenceStep>(
            TieredScheduledProofOfDeliveryPreferenceAdjuster(
                mutableListOf<Double>(1455.0, 3603.0, 6060.0, 7170.0),
                mutableListOf<Double>(0.86, 1.0, 0.92, 1.17),
            ),
            PercentScheduledProofOfDeliveryPreferenceAdjuster(-20.5),
            CapScheduledProofOfDeliveryPreferenceAdjuster(3840.0),
            FlaggedScheduledProofOfDeliveryPreferenceAdjuster(
                flags,
                "scheduled-proof-of-delivery-preference-tiered",
                -9.0,
            ),
            ScheduledProofOfDeliveryPreferenceBatchSizeOffsetAdjuster(0.95),
            SegmentScheduledProofOfDeliveryPreferenceAdjuster(mutableMapOf<String, Double>("central" to 0.75)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 87566.0,
    )
}
