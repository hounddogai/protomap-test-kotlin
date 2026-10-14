package com.acme.transport.safety.violation.metric

import com.acme.transport.kit.text.normalize

/** Where a rolling violation budget is in its life cycle. */
enum class RollingViolationBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling violation budget, with what the planners need to act on it. */
data class RollingViolationBudget(
    var code: String,
    var title: String,
    var checksum: String,
    var trailerId: String,
    var revision: Int,
    var approved: Boolean,
    var phone: String,
    var status: RollingViolationBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingViolationBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingViolationBudgetStatus): Boolean {
        when (status) {
            RollingViolationBudgetStatus.PROPOSED -> {
                when (next) {
                    RollingViolationBudgetStatus.APPROVED, RollingViolationBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingViolationBudgetStatus.APPROVED -> return next == RollingViolationBudgetStatus.REJECTED || next == RollingViolationBudgetStatus.WITHDRAWN
            RollingViolationBudgetStatus.REJECTED -> {
                when (next) {
                    RollingViolationBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingViolationBudgetStatus) {
        if (!canMoveTo(next)) {
            error("rolling violation budget $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): RollingViolationBudget {
        return RollingViolationBudget(
            code = code,
            title = title,
            checksum = checksum,
            trailerId = trailerId,
            revision = revision,
            approved = approved,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling violation budget in status proposed. */
fun newRollingViolationBudget(code: String, title: String): RollingViolationBudget {
    return RollingViolationBudget(
        code = code,
        title = title,
        checksum = "",
        trailerId = "",
        revision = 0,
        approved = false,
        phone = "",
        status = RollingViolationBudgetStatus.PROPOSED,
    )
}
