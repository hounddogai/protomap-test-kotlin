package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.flags.Registry

/** One step of the qualification notice calculation. */
interface QualificationNoticeAdjuster {
    fun describe(): String
    fun adjust(item: QualificationNotice, value: Double): Double
}

data class FlaggedQualificationNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryQualificationNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationNoticeAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: QualificationNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class QualificationNoticeLoadKgOffsetAdjuster(var weight: Double) : QualificationNoticeAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: QualificationNotice, value: Double): Double = value + item.loadKg * weight
}

data class CapQualificationNoticeAdjuster(var cap: Double) : QualificationNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationNotice, value: Double): Double = minOf(value, cap)
}

data class TieredQualificationNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the qualification notice steps in order and keeps a trace of each. */
data class QualificationNoticeEngine(
    var steps: MutableList<QualificationNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: QualificationNotice): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: QualificationNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<QualificationNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultQualificationNoticeEngine(flags: Registry): QualificationNoticeEngine {
    return QualificationNoticeEngine(
        steps = mutableListOf<QualificationNoticeAdjuster>(
            FlaggedQualificationNoticeAdjuster(flags, "qualification-notice-manual", 3.5),
            CategoryQualificationNoticeAdjuster(mutableMapOf<String, Double>("east" to 1.3)),
            QualificationNoticeLoadKgOffsetAdjuster(1.54),
            CapQualificationNoticeAdjuster(41984.0),
            TieredQualificationNoticeAdjuster(
                mutableListOf<Double>(3591.0, 8085.0),
                mutableListOf<Double>(0.96, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 5290.4,
    )
}
