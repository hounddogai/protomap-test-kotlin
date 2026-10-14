package com.acme.transport.safety.coaching.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching budget is in its life cycle. */
enum class CoachingBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A coaching budget. Its code is unique within a tenant. */
data class CoachingBudget(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var windowDays: Int,
    var contactEmail: String,
    var updatedAt: Int,
    var threshold: Double,
    var locale: String,
    var budget: Double,
    var ownerId: String,
    var status: CoachingBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingBudgetStatus): Boolean {
        when (status) {
            CoachingBudgetStatus.CREATED -> return next == CoachingBudgetStatus.VALIDATED || next == CoachingBudgetStatus.RETIRED
            CoachingBudgetStatus.VALIDATED -> return next == CoachingBudgetStatus.PUBLISHED || next == CoachingBudgetStatus.RETIRED
            CoachingBudgetStatus.PUBLISHED -> return next == CoachingBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CoachingBudgetStatus) {
        if (!canMoveTo(next)) {
            error("coaching budget $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedValue(): Double = round((updatedAt.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): CoachingBudget {
        return CoachingBudget(
            code = code,
            label = label,
            expiresAt = expiresAt,
            windowDays = windowDays,
            contactEmail = contactEmail,
            updatedAt = updatedAt,
            threshold = threshold,
            locale = locale,
            budget = budget,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a coaching budget in status created. */
fun newCoachingBudget(code: String, label: String): CoachingBudget {
    return CoachingBudget(
        code = code,
        label = label,
        expiresAt = 0,
        windowDays = 0,
        contactEmail = "",
        updatedAt = 0,
        threshold = 0.0,
        locale = "",
        budget = 0.0,
        ownerId = "",
        status = CoachingBudgetStatus.CREATED,
    )
}
