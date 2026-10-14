package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive emission factor assignment is in its life cycle. */
enum class AdaptiveEmissionFactorAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive emission factor assignment and its current status. */
data class AdaptiveEmissionFactorAssignment(
    var code: String,
    var label: String,
    var source: String,
    var graceDays: Int,
    var priority: Int,
    var ratio: Double,
    var tags: MutableList<String>,
    var active: Boolean,
    var status: AdaptiveEmissionFactorAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEmissionFactorAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEmissionFactorAssignmentStatus): Boolean {
        when (status) {
            AdaptiveEmissionFactorAssignmentStatus.PLANNED -> return next == AdaptiveEmissionFactorAssignmentStatus.IN_PROGRESS || next == AdaptiveEmissionFactorAssignmentStatus.CANCELLED
            AdaptiveEmissionFactorAssignmentStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveEmissionFactorAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveEmissionFactorAssignmentStatus.IN_PROGRESS -> return next == AdaptiveEmissionFactorAssignmentStatus.COMPLETED || next == AdaptiveEmissionFactorAssignmentStatus.CANCELLED
            AdaptiveEmissionFactorAssignmentStatus.COMPLETED -> return next == AdaptiveEmissionFactorAssignmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveEmissionFactorAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive emission factor assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive emission factor assignment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((ratio - graceDays.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveEmissionFactorAssignment {
        return AdaptiveEmissionFactorAssignment(
            code = code,
            label = label,
            source = source,
            graceDays = graceDays,
            priority = priority,
            ratio = ratio,
            tags = tags,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive emission factor assignment in status planned. */
fun newAdaptiveEmissionFactorAssignment(code: String, label: String): AdaptiveEmissionFactorAssignment {
    return AdaptiveEmissionFactorAssignment(
        code = code,
        label = label,
        source = "",
        graceDays = 0,
        priority = 0,
        ratio = 0.0,
        tags = mutableListOf<String>(),
        active = false,
        status = AdaptiveEmissionFactorAssignmentStatus.PLANNED,
    )
}
