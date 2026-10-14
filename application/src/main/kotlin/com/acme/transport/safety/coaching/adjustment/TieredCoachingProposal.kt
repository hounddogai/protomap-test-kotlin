package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered coaching proposal is in its life cycle. */
enum class TieredCoachingProposalStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered coaching proposal as the coaching team keeps it. */
data class TieredCoachingProposal(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var checksum: String,
    var retries: Int,
    var status: TieredCoachingProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCoachingProposalStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCoachingProposalStatus): Boolean {
        when (status) {
            TieredCoachingProposalStatus.NEW -> return next == TieredCoachingProposalStatus.FAILED
            TieredCoachingProposalStatus.QUEUED -> {
                when (next) {
                    TieredCoachingProposalStatus.RUNNING -> return true
                }
                return false
            }
            TieredCoachingProposalStatus.RUNNING -> {
                when (next) {
                    TieredCoachingProposalStatus.DONE, TieredCoachingProposalStatus.FAILED -> return true
                }
                return false
            }
            TieredCoachingProposalStatus.DONE -> {
                when (next) {
                    TieredCoachingProposalStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCoachingProposalStatus) {
        if (!canMoveTo(next)) {
            error("tiered coaching proposal $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round((updatedAt.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): TieredCoachingProposal {
        return TieredCoachingProposal(
            code = code,
            name = name,
            updatedAt = updatedAt,
            checksum = checksum,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered coaching proposal in status new. */
fun newTieredCoachingProposal(code: String, name: String): TieredCoachingProposal {
    return TieredCoachingProposal(
        code = code,
        name = name,
        updatedAt = 0,
        checksum = "",
        retries = 0,
        status = TieredCoachingProposalStatus.NEW,
    )
}
