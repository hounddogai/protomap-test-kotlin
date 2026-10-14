package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance proposal is in its life cycle. */
enum class ClearanceProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A clearance proposal as the clearance team keeps it. */
data class ClearanceProposal(
    var code: String,
    var title: String,
    var ownerId: String,
    var discount: Double,
    var durationMinutes: Int,
    var status: ClearanceProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceProposalStatus): Boolean {
        when (status) {
            ClearanceProposalStatus.CREATED -> {
                when (next) {
                    ClearanceProposalStatus.PUBLISHED -> return true
                }
                return false
            }
            ClearanceProposalStatus.VALIDATED -> {
                when (next) {
                    ClearanceProposalStatus.PUBLISHED, ClearanceProposalStatus.RETIRED -> return true
                }
                return false
            }
            ClearanceProposalStatus.PUBLISHED -> return next == ClearanceProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ClearanceProposalStatus) {
        if (!canMoveTo(next)) {
            error("clearance proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round((discount + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): ClearanceProposal {
        return ClearanceProposal(
            code = code,
            title = title,
            ownerId = ownerId,
            discount = discount,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a clearance proposal in status created. */
fun newClearanceProposal(code: String, title: String): ClearanceProposal {
    return ClearanceProposal(
        code = code,
        title = title,
        ownerId = "",
        discount = 0.0,
        durationMinutes = 0,
        status = ClearanceProposalStatus.CREATED,
    )
}
