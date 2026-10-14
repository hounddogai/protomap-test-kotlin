package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending qualification assignment calculation. */
interface PendingQualificationAssignmentStep {
    fun describe(): String
    fun adjust(item: PendingQualificationAssignment, value: Double): Double
}

data class LabelPendingQualificationAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingQualificationAssignmentStep {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingQualificationAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPendingQualificationAssignmentAdjuster(
    var minimum: Double,
) : PendingQualificationAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingQualificationAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPendingQualificationAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingQualificationAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingQualificationAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingQualificationAssignmentAdjuster(
    var step: Double,
) : PendingQualificationAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingQualificationAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the pending qualification assignment steps in order and keeps a trace of each. */
data class PendingQualificationAssignmentEngine(
    var steps: MutableList<PendingQualificationAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingQualificationAssignment): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingQualificationAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingQualificationAssignmentEngine(flags: Registry): PendingQualificationAssignmentEngine {
    return PendingQualificationAssignmentEngine(
        steps = mutableListOf<PendingQualificationAssignmentStep>(
            LabelPendingQualificationAssignmentAdjuster(
                mutableMapOf<String, Double>("central" to 1.28, "online" to 0.72),
            ),
            MinimumPendingQualificationAssignmentAdjuster(17.6),
            FlaggedPendingQualificationAssignmentAdjuster(
                flags,
                "pending-qualification-assignment-legacy",
                -9.0,
            ),
            RoundingPendingQualificationAssignmentAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 3028.6,
    )
}
