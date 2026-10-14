package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.text.normalize

/** Where a scheduled capacity proposal is in its life cycle. */
enum class ScheduledCapacityProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled capacity proposal. Its code is unique within a tenant. */
data class ScheduledCapacityProposal(
    var code: String,
    var label: String,
    var source: String,
    var sequence: Int,
    var shiftCode: String,
    var status: ScheduledCapacityProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCapacityProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCapacityProposalStatus): Boolean {
        when (status) {
            ScheduledCapacityProposalStatus.PLANNED -> {
                when (next) {
                    ScheduledCapacityProposalStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledCapacityProposalStatus.SCHEDULED -> {
                when (next) {
                    ScheduledCapacityProposalStatus.COMPLETED, ScheduledCapacityProposalStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledCapacityProposalStatus.IN_PROGRESS -> return next == ScheduledCapacityProposalStatus.COMPLETED || next == ScheduledCapacityProposalStatus.CANCELLED
            ScheduledCapacityProposalStatus.COMPLETED -> {
                when (next) {
                    ScheduledCapacityProposalStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCapacityProposalStatus) {
        if (!canMoveTo(next)) {
            error("scheduled capacity proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): ScheduledCapacityProposal {
        return ScheduledCapacityProposal(
            code = code,
            label = label,
            source = source,
            sequence = sequence,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scheduled capacity proposal in status planned. */
fun newScheduledCapacityProposal(code: String, label: String): ScheduledCapacityProposal {
    return ScheduledCapacityProposal(
        code = code,
        label = label,
        source = "",
        sequence = 0,
        shiftCode = "",
        status = ScheduledCapacityProposalStatus.PLANNED,
    )
}
