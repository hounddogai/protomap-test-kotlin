package com.acme.transport.safety.violation.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation proposal is in its life cycle. */
enum class ViolationProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A violation proposal. Its code is unique within a tenant. */
data class ViolationProposal(
    var code: String,
    var title: String,
    var phone: String,
    var region: String,
    var expiresAt: Int,
    var etaMinutes: Int,
    var status: ViolationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationProposalStatus): Boolean {
        when (status) {
            ViolationProposalStatus.OPEN -> {
                when (next) {
                    ViolationProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            ViolationProposalStatus.IN_REVIEW -> {
                when (next) {
                    ViolationProposalStatus.REOPENED -> return true
                }
                return false
            }
            ViolationProposalStatus.RESOLVED -> return next == ViolationProposalStatus.ARCHIVED
            ViolationProposalStatus.REOPENED -> return next == ViolationProposalStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ViolationProposalStatus) {
        if (!canMoveTo(next)) {
            error("violation proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round((etaMinutes.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ViolationProposal {
        return ViolationProposal(
            code = code,
            title = title,
            phone = phone,
            region = region,
            expiresAt = expiresAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a violation proposal in status open. */
fun newViolationProposal(code: String, title: String): ViolationProposal {
    return ViolationProposal(
        code = code,
        title = title,
        phone = "",
        region = "",
        expiresAt = 0,
        etaMinutes = 0,
        status = ViolationProposalStatus.OPEN,
    )
}
