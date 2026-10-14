package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.text.normalize

/** Where a legacy rate card assignment is in its life cycle. */
enum class LegacyRateCardAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy rate card assignment. Its code is unique within a tenant. */
data class LegacyRateCardAssignment(
    var code: String,
    var name: String,
    var notes: String,
    var category: String,
    var score: Double,
    var status: LegacyRateCardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRateCardAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRateCardAssignmentStatus): Boolean {
        when (status) {
            LegacyRateCardAssignmentStatus.NEW -> {
                when (next) {
                    LegacyRateCardAssignmentStatus.QUEUED, LegacyRateCardAssignmentStatus.RUNNING -> return true
                }
                return false
            }
            LegacyRateCardAssignmentStatus.QUEUED -> {
                when (next) {
                    LegacyRateCardAssignmentStatus.RUNNING, LegacyRateCardAssignmentStatus.DONE -> return true
                }
                return false
            }
            LegacyRateCardAssignmentStatus.RUNNING -> {
                when (next) {
                    LegacyRateCardAssignmentStatus.DONE -> return true
                }
                return false
            }
            LegacyRateCardAssignmentStatus.DONE -> return next == LegacyRateCardAssignmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyRateCardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy rate card assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = score

    fun snapshot(): LegacyRateCardAssignment {
        return LegacyRateCardAssignment(
            code = code,
            name = name,
            notes = notes,
            category = category,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy rate card assignment in status new. */
fun newLegacyRateCardAssignment(code: String, name: String): LegacyRateCardAssignment {
    return LegacyRateCardAssignment(
        code = code,
        name = name,
        notes = "",
        category = "",
        score = 0.0,
        status = LegacyRateCardAssignmentStatus.NEW,
    )
}
