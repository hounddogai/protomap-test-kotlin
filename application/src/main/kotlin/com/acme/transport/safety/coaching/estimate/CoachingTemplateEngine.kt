package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching template calculation. */
interface CoachingTemplateFactor {
    fun describe(): String
    fun adjust(item: CoachingTemplate, value: Double): Double
}

data class FlaggedCoachingTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCoachingTemplateAdjuster(var step: Double) : CoachingTemplateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingTemplate, value: Double): Double = roundTo(value, step)
}

data class CoachingTemplateCapacityOffsetAdjuster(var weight: Double) : CoachingTemplateFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: CoachingTemplate, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class PercentCoachingTemplateAdjuster(var percent: Double) : CoachingTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the coaching template steps in order and keeps a trace of each. */
data class CoachingTemplateEngine(
    var steps: MutableList<CoachingTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CoachingTemplate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCoachingTemplateEngine(flags: Registry): CoachingTemplateEngine {
    return CoachingTemplateEngine(
        steps = mutableListOf<CoachingTemplateFactor>(
            FlaggedCoachingTemplateAdjuster(flags, "coaching-template-tiered", 8.2),
            RoundingCoachingTemplateAdjuster(5.0),
            CoachingTemplateCapacityOffsetAdjuster(1.64),
            PercentCoachingTemplateAdjuster(17.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 21439.5,
    )
}
