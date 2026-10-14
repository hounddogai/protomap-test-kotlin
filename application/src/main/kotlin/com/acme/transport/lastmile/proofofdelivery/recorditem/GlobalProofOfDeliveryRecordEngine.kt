package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.flags.Registry

/** One step of the global proof of delivery record calculation. */
interface GlobalProofOfDeliveryRecordAdjuster {
    fun describe(): String
    fun adjust(item: GlobalProofOfDeliveryRecord, value: Double): Double
}

data class FlaggedGlobalProofOfDeliveryRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalProofOfDeliveryRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalProofOfDeliveryRecordOdometerOffsetAdjuster(
    var weight: Double,
) : GlobalProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: GlobalProofOfDeliveryRecord, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class PercentGlobalProofOfDeliveryRecordAdjuster(
    var percent: Double,
) : GlobalProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalProofOfDeliveryRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapGlobalProofOfDeliveryRecordAdjuster(var cap: Double) : GlobalProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalProofOfDeliveryRecord, value: Double): Double = minOf(value, cap)
}

/** Runs the global proof of delivery record steps in order and keeps a trace of each. */
data class GlobalProofOfDeliveryRecordEngine(
    var steps: MutableList<GlobalProofOfDeliveryRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalProofOfDeliveryRecord): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalProofOfDeliveryRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalProofOfDeliveryRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalProofOfDeliveryRecordEngine(flags: Registry): GlobalProofOfDeliveryRecordEngine {
    return GlobalProofOfDeliveryRecordEngine(
        steps = mutableListOf<GlobalProofOfDeliveryRecordAdjuster>(
            FlaggedGlobalProofOfDeliveryRecordAdjuster(flags, "global-proof-of-delivery-record-capped", 6.3),
            GlobalProofOfDeliveryRecordOdometerOffsetAdjuster(0.24),
            PercentGlobalProofOfDeliveryRecordAdjuster(17.2),
            CapGlobalProofOfDeliveryRecordAdjuster(33509.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 59997.1,
    )
}
