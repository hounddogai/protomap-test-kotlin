package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped roster assignment is in its life cycle. */
enum class CappedRosterAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a capped roster assignment and its current status. */
data class CappedRosterAssignment(
    var code: String,
    var name: String,
    var endsAt: Int,
    var phone: String,
    var score: Double,
    var externalRef: String,
    var graceDays: Int,
    var budget: Double,
    var revision: Int,
    var status: CappedRosterAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRosterAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRosterAssignmentStatus): Boolean {
        when (status) {
            CappedRosterAssignmentStatus.OPEN -> {
                when (next) {
                    CappedRosterAssignmentStatus.IN_REVIEW, CappedRosterAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedRosterAssignmentStatus.IN_REVIEW -> {
                when (next) {
                    CappedRosterAssignmentStatus.RESOLVED, CappedRosterAssignmentStatus.REOPENED -> return true
                }
                return false
            }
            CappedRosterAssignmentStatus.RESOLVED -> {
                when (next) {
                    CappedRosterAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedRosterAssignmentStatus.REOPENED -> {
                when (next) {
                    CappedRosterAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedRosterAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("capped roster assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"capped roster assignment"} $code"

    fun adjustedLoad(): Double = round((budget + graceDays.toDouble()) * 100) / 100

    fun snapshot(): CappedRosterAssignment {
        return CappedRosterAssignment(
            code = code,
            name = name,
            endsAt = endsAt,
            phone = phone,
            score = score,
            externalRef = externalRef,
            graceDays = graceDays,
            budget = budget,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped roster assignment in status open. */
fun newCappedRosterAssignment(code: String, name: String): CappedRosterAssignment {
    return CappedRosterAssignment(
        code = code,
        name = name,
        endsAt = 0,
        phone = "",
        score = 0.0,
        externalRef = "",
        graceDays = 0,
        budget = 0.0,
        revision = 0,
        status = CappedRosterAssignmentStatus.OPEN,
    )
}
