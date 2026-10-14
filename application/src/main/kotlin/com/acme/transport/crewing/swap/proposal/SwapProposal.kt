package com.acme.transport.crewing.swap.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The swap proposal records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap proposal is in its life cycle. */
enum class SwapProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A swap proposal. Its code is unique within a tenant. */
data class SwapProposal(
    var code: String,
    var title: String,
    var category: String,
    var locale: String,
    var quantity: Int,
    var updatedAt: Int,
    var retries: Int,
    var notes: String,
    var margin: Double,
    var odometer: Int,
    var sequence: Int,
    var status: SwapProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapProposalStatus): Boolean {
        when (status) {
            SwapProposalStatus.CREATED -> {
                when (next) {
                    SwapProposalStatus.PUBLISHED, SwapProposalStatus.RETIRED -> return true
                }
                return false
            }
            SwapProposalStatus.VALIDATED -> {
                when (next) {
                    SwapProposalStatus.PUBLISHED, SwapProposalStatus.RETIRED -> return true
                }
                return false
            }
            SwapProposalStatus.PUBLISHED -> {
                when (next) {
                    SwapProposalStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapProposalStatus) {
        if (!canMoveTo(next)) {
            error("swap proposal $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"swap proposal"} $code"

    fun projectedLoad(): Double = round(updatedAt.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): SwapProposal {
        return SwapProposal(
            code = code,
            title = title,
            category = category,
            locale = locale,
            quantity = quantity,
            updatedAt = updatedAt,
            retries = retries,
            notes = notes,
            margin = margin,
            odometer = odometer,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a swap proposal in status created. */
fun newSwapProposal(code: String, title: String): SwapProposal {
    return SwapProposal(
        code = code,
        title = title,
        category = "",
        locale = "",
        quantity = 0,
        updatedAt = 0,
        retries = 0,
        notes = "",
        margin = 0.0,
        odometer = 0,
        sequence = 0,
        status = SwapProposalStatus.CREATED,
    )
}
