package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.text.normalize

/** Where a inspection checkpoint is in its life cycle. */
enum class InspectionCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a inspection checkpoint and its current status. */
data class InspectionCheckpoint(
    var code: String,
    var title: String,
    var summary: String,
    var externalRef: String,
    var reviewer: String,
    var currency: String,
    var contactEmail: String,
    var amount: Double,
    var status: InspectionCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionCheckpointStatus): Boolean {
        when (status) {
            InspectionCheckpointStatus.PROPOSED -> return next == InspectionCheckpointStatus.APPROVED
            InspectionCheckpointStatus.APPROVED -> {
                when (next) {
                    InspectionCheckpointStatus.REJECTED -> return true
                }
                return false
            }
            InspectionCheckpointStatus.REJECTED -> {
                when (next) {
                    InspectionCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("inspection checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = amount

    fun snapshot(): InspectionCheckpoint {
        return InspectionCheckpoint(
            code = code,
            title = title,
            summary = summary,
            externalRef = externalRef,
            reviewer = reviewer,
            currency = currency,
            contactEmail = contactEmail,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection checkpoint in status proposed. */
fun newInspectionCheckpoint(code: String, title: String): InspectionCheckpoint {
    return InspectionCheckpoint(
        code = code,
        title = title,
        summary = "",
        externalRef = "",
        reviewer = "",
        currency = "",
        contactEmail = "",
        amount = 0.0,
        status = InspectionCheckpointStatus.PROPOSED,
    )
}
