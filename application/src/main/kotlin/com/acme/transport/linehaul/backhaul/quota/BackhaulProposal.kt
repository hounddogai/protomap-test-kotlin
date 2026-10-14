package com.acme.transport.linehaul.backhaul.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul proposal is in its life cycle. */
enum class BackhaulProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One backhaul proposal, with what the planners need to act on it. */
data class BackhaulProposal(
    var code: String,
    var name: String,
    var trailerId: String,
    var cost: Double,
    var label: String,
    var expiresAt: Int,
    var visible: Boolean,
    var ceiling: Double,
    var notes: String,
    var status: BackhaulProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulProposalStatus): Boolean {
        when (status) {
            BackhaulProposalStatus.DRAFT -> return next == BackhaulProposalStatus.ACTIVE
            BackhaulProposalStatus.PENDING -> return next == BackhaulProposalStatus.ACTIVE || next == BackhaulProposalStatus.SUSPENDED
            BackhaulProposalStatus.ACTIVE -> return next == BackhaulProposalStatus.SUSPENDED
            BackhaulProposalStatus.SUSPENDED -> {
                when (next) {
                    BackhaulProposalStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulProposalStatus) {
        if (!canMoveTo(next)) {
            error("backhaul proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"backhaul proposal"} $code"

    fun projectedLoad(): Double = round(ceiling * expiresAt.toDouble() * 100) / 100

    fun snapshot(): BackhaulProposal {
        return BackhaulProposal(
            code = code,
            name = name,
            trailerId = trailerId,
            cost = cost,
            label = label,
            expiresAt = expiresAt,
            visible = visible,
            ceiling = ceiling,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a backhaul proposal in status draft. */
fun newBackhaulProposal(code: String, name: String): BackhaulProposal {
    return BackhaulProposal(
        code = code,
        name = name,
        trailerId = "",
        cost = 0.0,
        label = "",
        expiresAt = 0,
        visible = false,
        ceiling = 0.0,
        notes = "",
        status = BackhaulProposalStatus.DRAFT,
    )
}
