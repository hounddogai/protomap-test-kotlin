package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss assignment is in its life cycle. */
enum class NearMissAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One near miss assignment, with what the planners need to act on it. */
data class NearMissAssignment(
    var code: String,
    var title: String,
    var endsAt: Int,
    var description: String,
    var amount: Double,
    var depot: String,
    var durationMinutes: Int,
    var budget: Double,
    var locale: String,
    var status: NearMissAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissAssignmentStatus): Boolean {
        when (status) {
            NearMissAssignmentStatus.DRAFT -> return next == NearMissAssignmentStatus.SUSPENDED
            NearMissAssignmentStatus.PENDING -> return next == NearMissAssignmentStatus.SUSPENDED
            NearMissAssignmentStatus.ACTIVE -> {
                when (next) {
                    NearMissAssignmentStatus.SUSPENDED, NearMissAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            NearMissAssignmentStatus.SUSPENDED -> return next == NearMissAssignmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: NearMissAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("near miss assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netValue(): Double = round(budget * amount * 100) / 100

    fun snapshot(): NearMissAssignment {
        return NearMissAssignment(
            code = code,
            title = title,
            endsAt = endsAt,
            description = description,
            amount = amount,
            depot = depot,
            durationMinutes = durationMinutes,
            budget = budget,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a near miss assignment in status draft. */
fun newNearMissAssignment(code: String, title: String): NearMissAssignment {
    return NearMissAssignment(
        code = code,
        title = title,
        endsAt = 0,
        description = "",
        amount = 0.0,
        depot = "",
        durationMinutes = 0,
        budget = 0.0,
        locale = "",
        status = NearMissAssignmentStatus.DRAFT,
    )
}
