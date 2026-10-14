package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched duty period proposal records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a batched duty period proposal is in its life cycle. */
enum class BatchedDutyPeriodProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One batched duty period proposal, with what the planners need to act on it. */
data class BatchedDutyPeriodProposal(
    var code: String,
    var label: String,
    var channel: String,
    var shiftCode: String,
    var graceDays: Int,
    var pinned: Boolean,
    var vehicleId: String,
    var odometer: Int,
    var approved: Boolean,
    var status: BatchedDutyPeriodProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDutyPeriodProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDutyPeriodProposalStatus): Boolean {
        when (status) {
            BatchedDutyPeriodProposalStatus.REQUESTED -> return next == BatchedDutyPeriodProposalStatus.GRANTED || next == BatchedDutyPeriodProposalStatus.REVOKED
            BatchedDutyPeriodProposalStatus.GRANTED -> return next == BatchedDutyPeriodProposalStatus.EXPIRED
            BatchedDutyPeriodProposalStatus.REVOKED -> return next == BatchedDutyPeriodProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedDutyPeriodProposalStatus) {
        if (!canMoveTo(next)) {
            error("batched duty period proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round(odometer.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): BatchedDutyPeriodProposal {
        return BatchedDutyPeriodProposal(
            code = code,
            label = label,
            channel = channel,
            shiftCode = shiftCode,
            graceDays = graceDays,
            pinned = pinned,
            vehicleId = vehicleId,
            odometer = odometer,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched duty period proposal in status requested. */
fun newBatchedDutyPeriodProposal(code: String, label: String): BatchedDutyPeriodProposal {
    return BatchedDutyPeriodProposal(
        code = code,
        label = label,
        channel = "",
        shiftCode = "",
        graceDays = 0,
        pinned = false,
        vehicleId = "",
        odometer = 0,
        approved = false,
        status = BatchedDutyPeriodProposalStatus.REQUESTED,
    )
}
