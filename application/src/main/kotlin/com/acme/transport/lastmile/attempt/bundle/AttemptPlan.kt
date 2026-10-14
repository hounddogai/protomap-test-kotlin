package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt plan is in its life cycle. */
enum class AttemptPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A attempt plan as the attempt team keeps it. */
data class AttemptPlan(
    var code: String,
    var name: String,
    var segment: String,
    var maximum: Double,
    var discount: Double,
    var status: AttemptPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptPlanStatus): Boolean {
        when (status) {
            AttemptPlanStatus.NEW -> {
                when (next) {
                    AttemptPlanStatus.QUEUED -> return true
                }
                return false
            }
            AttemptPlanStatus.QUEUED -> {
                when (next) {
                    AttemptPlanStatus.DONE -> return true
                }
                return false
            }
            AttemptPlanStatus.RUNNING -> return next == AttemptPlanStatus.DONE
            AttemptPlanStatus.DONE -> {
                when (next) {
                    AttemptPlanStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptPlanStatus) {
        if (!canMoveTo(next)) {
            error("attempt plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((discount - maximum) * 100) / 100

    fun snapshot(): AttemptPlan {
        return AttemptPlan(
            code = code,
            name = name,
            segment = segment,
            maximum = maximum,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a attempt plan in status new. */
fun newAttemptPlan(code: String, name: String): AttemptPlan {
    return AttemptPlan(
        code = code,
        name = name,
        segment = "",
        maximum = 0.0,
        discount = 0.0,
        status = AttemptPlanStatus.NEW,
    )
}
