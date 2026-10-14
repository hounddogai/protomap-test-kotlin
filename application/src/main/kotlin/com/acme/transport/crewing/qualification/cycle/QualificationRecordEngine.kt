package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification record calculation. */
interface QualificationRecordAdjuster {
    fun describe(): String
    fun adjust(item: QualificationRecord, value: Double): Double
}

data class PercentQualificationRecordAdjuster(var percent: Double) : QualificationRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingQualificationRecordAdjuster(var step: Double) : QualificationRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationRecord, value: Double): Double = roundTo(value, step)
}

data class QualificationRecordVersionOffsetAdjuster(var weight: Double) : QualificationRecordAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: QualificationRecord, value: Double): Double = value + item.version.toDouble() * weight
}

data class FlaggedQualificationRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the qualification record steps in order and keeps a trace of each. */
data class QualificationRecordEngine(
    var steps: MutableList<QualificationRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: QualificationRecord): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultQualificationRecordEngine(flags: Registry): QualificationRecordEngine {
    return QualificationRecordEngine(
        steps = mutableListOf<QualificationRecordAdjuster>(
            PercentQualificationRecordAdjuster(-7.7),
            RoundingQualificationRecordAdjuster(5.0),
            QualificationRecordVersionOffsetAdjuster(1.33),
            FlaggedQualificationRecordAdjuster(flags, "qualification-record-regional", -0.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 88252.8,
    )
}
