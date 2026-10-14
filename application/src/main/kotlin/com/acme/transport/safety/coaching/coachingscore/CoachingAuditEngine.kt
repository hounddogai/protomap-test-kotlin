package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching audit calculation. */
interface CoachingAuditFactor {
    fun describe(): String
    fun adjust(item: CoachingAudit, value: Double): Double
}

data class TitleCoachingAuditAdjuster(var factors: MutableMap<String, Double>) : CoachingAuditFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: CoachingAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CoachingAuditCreatedAtOffsetAdjuster(var weight: Double) : CoachingAuditFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: CoachingAudit, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class RoundingCoachingAuditAdjuster(var step: Double) : CoachingAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingAudit, value: Double): Double = roundTo(value, step)
}

data class FlaggedCoachingAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the coaching audit steps in order and keeps a trace of each. */
data class CoachingAuditEngine(
    var steps: MutableList<CoachingAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingAudit): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingAuditEngine(flags: Registry): CoachingAuditEngine {
    return CoachingAuditEngine(
        steps = mutableListOf<CoachingAuditFactor>(
            TitleCoachingAuditAdjuster(mutableMapOf<String, Double>("west" to 0.72, "central" to 0.89)),
            CoachingAuditCreatedAtOffsetAdjuster(1.21),
            RoundingCoachingAuditAdjuster(5.0),
            FlaggedCoachingAuditAdjuster(flags, "coaching-audit-seasonal", -0.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 74843.9,
    )
}
