package com.acme.transport.fleetops.inspection.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict inspection proposal is in its life cycle. */
enum class StrictInspectionProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a strict inspection proposal and its current status. */
data class StrictInspectionProposal(
    var code: String,
    var name: String,
    var createdAt: Int,
    var cost: Double,
    var ceiling: Double,
    var ownerId: String,
    var status: StrictInspectionProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictInspectionProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictInspectionProposalStatus): Boolean {
        when (status) {
            StrictInspectionProposalStatus.IDLE -> return next == StrictInspectionProposalStatus.WARMING || next == StrictInspectionProposalStatus.DRAINING
            StrictInspectionProposalStatus.WARMING -> {
                when (next) {
                    StrictInspectionProposalStatus.STOPPED -> return true
                }
                return false
            }
            StrictInspectionProposalStatus.READY -> {
                when (next) {
                    StrictInspectionProposalStatus.STOPPED -> return true
                }
                return false
            }
            StrictInspectionProposalStatus.DRAINING -> return next == StrictInspectionProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictInspectionProposalStatus) {
        if (!canMoveTo(next)) {
            error("strict inspection proposal $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict inspection proposal"} $code"

    fun effectiveTotal(): Double = round(createdAt.toDouble() * cost * 100) / 100

    fun snapshot(): StrictInspectionProposal {
        return StrictInspectionProposal(
            code = code,
            name = name,
            createdAt = createdAt,
            cost = cost,
            ceiling = ceiling,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict inspection proposal in status idle. */
fun newStrictInspectionProposal(code: String, name: String): StrictInspectionProposal {
    return StrictInspectionProposal(
        code = code,
        name = name,
        createdAt = 0,
        cost = 0.0,
        ceiling = 0.0,
        ownerId = "",
        status = StrictInspectionProposalStatus.IDLE,
    )
}
