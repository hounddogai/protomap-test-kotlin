package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient swap proposal is in its life cycle. */
enum class LenientSwapProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lenient swap proposal and its current status. */
data class LenientSwapProposal(
    var code: String,
    var name: String,
    var createdAt: Int,
    var threshold: Double,
    var weight: Double,
    var maximum: Double,
    var status: LenientSwapProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientSwapProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientSwapProposalStatus): Boolean {
        when (status) {
            LenientSwapProposalStatus.OPEN -> {
                when (next) {
                    LenientSwapProposalStatus.RESOLVED, LenientSwapProposalStatus.REOPENED -> return true
                }
                return false
            }
            LenientSwapProposalStatus.IN_REVIEW -> return next == LenientSwapProposalStatus.RESOLVED || next == LenientSwapProposalStatus.ARCHIVED
            LenientSwapProposalStatus.RESOLVED -> return next == LenientSwapProposalStatus.ARCHIVED
            LenientSwapProposalStatus.REOPENED -> {
                when (next) {
                    LenientSwapProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientSwapProposalStatus) {
        if (!canMoveTo(next)) {
            error("lenient swap proposal $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedTotal(): Double = round((threshold - weight) * 100) / 100

    fun snapshot(): LenientSwapProposal {
        return LenientSwapProposal(
            code = code,
            name = name,
            createdAt = createdAt,
            threshold = threshold,
            weight = weight,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient swap proposal in status open. */
fun newLenientSwapProposal(code: String, name: String): LenientSwapProposal {
    return LenientSwapProposal(
        code = code,
        name = name,
        createdAt = 0,
        threshold = 0.0,
        weight = 0.0,
        maximum = 0.0,
        status = LenientSwapProposalStatus.OPEN,
    )
}
