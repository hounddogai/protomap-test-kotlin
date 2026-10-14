package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched incident proposal is in its life cycle. */
enum class BatchedIncidentProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched incident proposal. Its code is unique within a tenant. */
data class BatchedIncidentProposal(
    var code: String,
    var label: String,
    var weight: Double,
    var distanceKm: Double,
    var phone: String,
    var windowDays: Int,
    var priority: Int,
    var source: String,
    var trailerId: String,
    var maximum: Double,
    var floorValue: Double,
    var status: BatchedIncidentProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedIncidentProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedIncidentProposalStatus): Boolean {
        when (status) {
            BatchedIncidentProposalStatus.OPEN -> {
                when (next) {
                    BatchedIncidentProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedIncidentProposalStatus.IN_REVIEW -> return next == BatchedIncidentProposalStatus.RESOLVED
            BatchedIncidentProposalStatus.RESOLVED -> {
                when (next) {
                    BatchedIncidentProposalStatus.REOPENED -> return true
                }
                return false
            }
            BatchedIncidentProposalStatus.REOPENED -> {
                when (next) {
                    BatchedIncidentProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedIncidentProposalStatus) {
        if (!canMoveTo(next)) {
            error("batched incident proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round((maximum - distanceKm) * 100) / 100

    fun snapshot(): BatchedIncidentProposal {
        return BatchedIncidentProposal(
            code = code,
            label = label,
            weight = weight,
            distanceKm = distanceKm,
            phone = phone,
            windowDays = windowDays,
            priority = priority,
            source = source,
            trailerId = trailerId,
            maximum = maximum,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched incident proposal in status open. */
fun newBatchedIncidentProposal(code: String, label: String): BatchedIncidentProposal {
    return BatchedIncidentProposal(
        code = code,
        label = label,
        weight = 0.0,
        distanceKm = 0.0,
        phone = "",
        windowDays = 0,
        priority = 0,
        source = "",
        trailerId = "",
        maximum = 0.0,
        floorValue = 0.0,
        status = BatchedIncidentProposalStatus.OPEN,
    )
}
