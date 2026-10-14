package com.acme.transport.crewing.qualification.plan

/** One step of the primary qualification record calculation. */
interface PrimaryQualificationRecordModifier {
    fun describe(): String
    fun adjust(item: PrimaryQualificationRecord, value: Double): Double
}

data class MinimumPrimaryQualificationRecordAdjuster(var minimum: Double) : PrimaryQualificationRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryQualificationRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPrimaryQualificationRecordAdjuster(var percent: Double) : PrimaryQualificationRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryQualificationRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPrimaryQualificationRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryQualificationRecordModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryQualificationRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPrimaryQualificationRecordAdjuster(var cap: Double) : PrimaryQualificationRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryQualificationRecord, value: Double): Double = minOf(value, cap)
}

data class PrimaryQualificationRecordRetriesOffsetAdjuster(
    var weight: Double,
) : PrimaryQualificationRecordModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: PrimaryQualificationRecord, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class RegionPrimaryQualificationRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryQualificationRecordModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: PrimaryQualificationRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary qualification record steps in order and keeps a trace of each. */
data class PrimaryQualificationRecordEngine(
    var steps: MutableList<PrimaryQualificationRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryQualificationRecord): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryQualificationRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryQualificationRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryQualificationRecordEngine(): PrimaryQualificationRecordEngine {
    return PrimaryQualificationRecordEngine(
        steps = mutableListOf<PrimaryQualificationRecordModifier>(
            MinimumPrimaryQualificationRecordAdjuster(24.7),
            PercentPrimaryQualificationRecordAdjuster(-17.7),
            TieredPrimaryQualificationRecordAdjuster(
                mutableListOf<Double>(1661.0, 6014.0, 7610.0, 8207.0),
                mutableListOf<Double>(1.2, 1.17, 1.16, 0.81),
            ),
            CapPrimaryQualificationRecordAdjuster(27920.0),
            PrimaryQualificationRecordRetriesOffsetAdjuster(1.76),
            RegionPrimaryQualificationRecordAdjuster(
                mutableMapOf<String, Double>("north" to 0.84, "south" to 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 25722.1,
    )
}
