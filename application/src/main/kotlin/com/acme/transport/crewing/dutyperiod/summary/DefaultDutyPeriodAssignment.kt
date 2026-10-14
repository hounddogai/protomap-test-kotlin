package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default duty period assignment is in its life cycle. */
enum class DefaultDutyPeriodAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One default duty period assignment, with what the planners need to act on it. */
data class DefaultDutyPeriodAssignment(
    var code: String,
    var label: String,
    var reviewer: String,
    var retries: Int,
    var distanceKm: Double,
    var endsAt: Int,
    var pallets: Int,
    var graceDays: Int,
    var createdAt: Int,
    var quantity: Int,
    var status: DefaultDutyPeriodAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDutyPeriodAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDutyPeriodAssignmentStatus): Boolean {
        when (status) {
            DefaultDutyPeriodAssignmentStatus.DRAFT -> return next == DefaultDutyPeriodAssignmentStatus.PENDING || next == DefaultDutyPeriodAssignmentStatus.SUSPENDED
            DefaultDutyPeriodAssignmentStatus.PENDING -> return next == DefaultDutyPeriodAssignmentStatus.CLOSED
            DefaultDutyPeriodAssignmentStatus.ACTIVE -> {
                when (next) {
                    DefaultDutyPeriodAssignmentStatus.SUSPENDED, DefaultDutyPeriodAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            DefaultDutyPeriodAssignmentStatus.SUSPENDED -> return next == DefaultDutyPeriodAssignmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultDutyPeriodAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("default duty period assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default duty period assignment"} $code"

    fun projectedTotal(): Double = round(endsAt.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): DefaultDutyPeriodAssignment {
        return DefaultDutyPeriodAssignment(
            code = code,
            label = label,
            reviewer = reviewer,
            retries = retries,
            distanceKm = distanceKm,
            endsAt = endsAt,
            pallets = pallets,
            graceDays = graceDays,
            createdAt = createdAt,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default duty period assignment in status draft. */
fun newDefaultDutyPeriodAssignment(code: String, label: String): DefaultDutyPeriodAssignment {
    return DefaultDutyPeriodAssignment(
        code = code,
        label = label,
        reviewer = "",
        retries = 0,
        distanceKm = 0.0,
        endsAt = 0,
        pallets = 0,
        graceDays = 0,
        createdAt = 0,
        quantity = 0,
        status = DefaultDutyPeriodAssignmentStatus.DRAFT,
    )
}
