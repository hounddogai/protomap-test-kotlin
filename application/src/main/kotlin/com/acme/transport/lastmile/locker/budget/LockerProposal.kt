package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker proposal is in its life cycle. */
enum class LockerProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One locker proposal, with what the planners need to act on it. */
data class LockerProposal(
    var code: String,
    var title: String,
    var discount: Double,
    var retries: Int,
    var depot: String,
    var name: String,
    var phone: String,
    var ratio: Double,
    var sequence: Int,
    var status: LockerProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerProposalStatus): Boolean {
        when (status) {
            LockerProposalStatus.CREATED -> {
                when (next) {
                    LockerProposalStatus.PUBLISHED, LockerProposalStatus.RETIRED -> return true
                }
                return false
            }
            LockerProposalStatus.VALIDATED -> return next == LockerProposalStatus.PUBLISHED || next == LockerProposalStatus.RETIRED
            LockerProposalStatus.PUBLISHED -> return next == LockerProposalStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LockerProposalStatus) {
        if (!canMoveTo(next)) {
            error("locker proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker proposal"} $code"

    fun adjustedTotal(): Double = round(ratio * retries.toDouble() * 100) / 100

    fun snapshot(): LockerProposal {
        return LockerProposal(
            code = code,
            title = title,
            discount = discount,
            retries = retries,
            depot = depot,
            name = name,
            phone = phone,
            ratio = ratio,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a locker proposal in status created. */
fun newLockerProposal(code: String, title: String): LockerProposal {
    return LockerProposal(
        code = code,
        title = title,
        discount = 0.0,
        retries = 0,
        depot = "",
        name = "",
        phone = "",
        ratio = 0.0,
        sequence = 0,
        status = LockerProposalStatus.CREATED,
    )
}
