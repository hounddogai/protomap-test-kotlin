package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching rollup is in its life cycle. */
enum class CoachingRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a coaching rollup and its current status. */
data class CoachingRollup(
    var code: String,
    var title: String,
    var weight: Double,
    var region: String,
    var budget: Double,
    var contactEmail: String,
    var status: CoachingRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingRollupStatus): Boolean {
        when (status) {
            CoachingRollupStatus.PLANNED -> return next == CoachingRollupStatus.SCHEDULED || next == CoachingRollupStatus.COMPLETED
            CoachingRollupStatus.SCHEDULED -> {
                when (next) {
                    CoachingRollupStatus.CANCELLED -> return true
                }
                return false
            }
            CoachingRollupStatus.IN_PROGRESS -> return next == CoachingRollupStatus.CANCELLED
            CoachingRollupStatus.COMPLETED -> {
                when (next) {
                    CoachingRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingRollupStatus) {
        if (!canMoveTo(next)) {
            error("coaching rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round((weight + budget) * 100) / 100

    fun snapshot(): CoachingRollup {
        return CoachingRollup(
            code = code,
            title = title,
            weight = weight,
            region = region,
            budget = budget,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a coaching rollup in status planned. */
fun newCoachingRollup(code: String, title: String): CoachingRollup {
    return CoachingRollup(
        code = code,
        title = title,
        weight = 0.0,
        region = "",
        budget = 0.0,
        contactEmail = "",
        status = CoachingRollupStatus.PLANNED,
    )
}
