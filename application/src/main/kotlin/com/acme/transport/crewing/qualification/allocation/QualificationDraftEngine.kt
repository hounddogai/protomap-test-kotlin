package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification draft calculation. */
interface QualificationDraftStep {
    fun describe(): String
    fun adjust(item: QualificationDraft, value: Double): Double
}

data class QualificationDraftEtaMinutesOffsetAdjuster(var weight: Double) : QualificationDraftStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: QualificationDraft, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class FlaggedQualificationDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : QualificationDraftStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: QualificationDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredQualificationDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingQualificationDraftAdjuster(var step: Double) : QualificationDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationDraft, value: Double): Double = roundTo(value, step)
}

data class PercentQualificationDraftAdjuster(var percent: Double) : QualificationDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the qualification draft steps in order and keeps a trace of each. */
data class QualificationDraftEngine(
    var steps: MutableList<QualificationDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: QualificationDraft): Double {
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

    fun evaluateAll(items: MutableList<QualificationDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultQualificationDraftEngine(flags: Registry): QualificationDraftEngine {
    return QualificationDraftEngine(
        steps = mutableListOf<QualificationDraftStep>(
            QualificationDraftEtaMinutesOffsetAdjuster(1.93),
            FlaggedQualificationDraftAdjuster(flags, "qualification-draft-batched", -2.1),
            TieredQualificationDraftAdjuster(
                mutableListOf<Double>(201.0, 2248.0, 4205.0),
                mutableListOf<Double>(1.17, 1.0, 0.91),
            ),
            RoundingQualificationDraftAdjuster(0.25),
            PercentQualificationDraftAdjuster(-5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 35381.7,
    )
}
