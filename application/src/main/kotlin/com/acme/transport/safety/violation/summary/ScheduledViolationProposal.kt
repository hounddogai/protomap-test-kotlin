package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled violation proposal is in its life cycle. */
enum class ScheduledViolationProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled violation proposal. Its code is unique within a tenant. */
data class ScheduledViolationProposal(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var cost: Double,
    var weight: Double,
    var status: ScheduledViolationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledViolationProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledViolationProposalStatus): Boolean {
        when (status) {
            ScheduledViolationProposalStatus.PLANNED -> return next == ScheduledViolationProposalStatus.SCHEDULED
            ScheduledViolationProposalStatus.SCHEDULED -> return next == ScheduledViolationProposalStatus.COMPLETED || next == ScheduledViolationProposalStatus.CANCELLED
            ScheduledViolationProposalStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledViolationProposalStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledViolationProposalStatus.COMPLETED -> return next == ScheduledViolationProposalStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledViolationProposalStatus) {
        if (!canMoveTo(next)) {
            error("scheduled violation proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveValue(): Double = round((weight + cost) * 100) / 100

    fun snapshot(): ScheduledViolationProposal {
        return ScheduledViolationProposal(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            cost = cost,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled violation proposal in status planned. */
fun newScheduledViolationProposal(code: String, title: String): ScheduledViolationProposal {
    return ScheduledViolationProposal(
        code = code,
        title = title,
        fuelLiters = 0.0,
        cost = 0.0,
        weight = 0.0,
        status = ScheduledViolationProposalStatus.PLANNED,
    )
}
