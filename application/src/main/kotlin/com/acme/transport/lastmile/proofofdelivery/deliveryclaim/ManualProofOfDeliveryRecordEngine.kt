package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

/** One step of the manual proof of delivery record calculation. */
interface ManualProofOfDeliveryRecordStep {
    fun describe(): String
    fun adjust(item: ManualProofOfDeliveryRecord, value: Double): Double
}

data class ManualProofOfDeliveryRecordVersionOffsetAdjuster(
    var weight: Double,
) : ManualProofOfDeliveryRecordStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: ManualProofOfDeliveryRecord, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class CapManualProofOfDeliveryRecordAdjuster(var cap: Double) : ManualProofOfDeliveryRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualProofOfDeliveryRecord, value: Double): Double = minOf(value, cap)
}

data class SourceManualProofOfDeliveryRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualProofOfDeliveryRecordStep {
    override fun describe(): String = "by source"

    override fun adjust(item: ManualProofOfDeliveryRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual proof of delivery record steps in order and keeps a trace of each. */
data class ManualProofOfDeliveryRecordEngine(
    var steps: MutableList<ManualProofOfDeliveryRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualProofOfDeliveryRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualProofOfDeliveryRecordStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualProofOfDeliveryRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualProofOfDeliveryRecordEngine(): ManualProofOfDeliveryRecordEngine {
    return ManualProofOfDeliveryRecordEngine(
        steps = mutableListOf<ManualProofOfDeliveryRecordStep>(
            ManualProofOfDeliveryRecordVersionOffsetAdjuster(0.51),
            CapManualProofOfDeliveryRecordAdjuster(43809.0),
            SourceManualProofOfDeliveryRecordAdjuster(mutableMapOf<String, Double>("online" to 1.06)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 93274.3,
    )
}
