package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual dispute assignment records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a manual dispute assignment is in its life cycle. */
enum class ManualDisputeAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual dispute assignment, with what the planners need to act on it. */
data class ManualDisputeAssignment(
    var code: String,
    var label: String,
    var channel: String,
    var loadKg: Double,
    var reviewer: String,
    var etaMinutes: Int,
    var archived: Boolean,
    var pallets: Int,
    var timezone: String,
    var discount: Double,
    var revision: Int,
    var status: ManualDisputeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDisputeAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDisputeAssignmentStatus): Boolean {
        when (status) {
            ManualDisputeAssignmentStatus.PROPOSED -> return next == ManualDisputeAssignmentStatus.APPROVED
            ManualDisputeAssignmentStatus.APPROVED -> {
                when (next) {
                    ManualDisputeAssignmentStatus.REJECTED, ManualDisputeAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualDisputeAssignmentStatus.REJECTED -> {
                when (next) {
                    ManualDisputeAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDisputeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("manual dispute assignment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedVolume(): Double = round(loadKg * discount * 100) / 100

    fun snapshot(): ManualDisputeAssignment {
        return ManualDisputeAssignment(
            code = code,
            label = label,
            channel = channel,
            loadKg = loadKg,
            reviewer = reviewer,
            etaMinutes = etaMinutes,
            archived = archived,
            pallets = pallets,
            timezone = timezone,
            discount = discount,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a manual dispute assignment in status proposed. */
fun newManualDisputeAssignment(code: String, label: String): ManualDisputeAssignment {
    return ManualDisputeAssignment(
        code = code,
        label = label,
        channel = "",
        loadKg = 0.0,
        reviewer = "",
        etaMinutes = 0,
        archived = false,
        pallets = 0,
        timezone = "",
        discount = 0.0,
        revision = 0,
        status = ManualDisputeAssignmentStatus.PROPOSED,
    )
}
