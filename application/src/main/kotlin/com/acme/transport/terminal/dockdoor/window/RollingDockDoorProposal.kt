package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling dock door proposal is in its life cycle. */
enum class RollingDockDoorProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rolling dock door proposal and its current status. */
data class RollingDockDoorProposal(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var revision: Int,
    var currency: String,
    var startsAt: Int,
    var status: RollingDockDoorProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDockDoorProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDockDoorProposalStatus): Boolean {
        when (status) {
            RollingDockDoorProposalStatus.PLANNED -> return next == RollingDockDoorProposalStatus.SCHEDULED || next == RollingDockDoorProposalStatus.CANCELLED
            RollingDockDoorProposalStatus.SCHEDULED -> {
                when (next) {
                    RollingDockDoorProposalStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RollingDockDoorProposalStatus.IN_PROGRESS -> {
                when (next) {
                    RollingDockDoorProposalStatus.COMPLETED, RollingDockDoorProposalStatus.CANCELLED -> return true
                }
                return false
            }
            RollingDockDoorProposalStatus.COMPLETED -> {
                when (next) {
                    RollingDockDoorProposalStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDockDoorProposalStatus) {
        if (!canMoveTo(next)) {
            error("rolling dock door proposal $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"rolling dock door proposal"} $code"

    fun netValue(): Double = round((revision.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): RollingDockDoorProposal {
        return RollingDockDoorProposal(
            code = code,
            name = name,
            pinned = pinned,
            revision = revision,
            currency = currency,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling dock door proposal in status planned. */
fun newRollingDockDoorProposal(code: String, name: String): RollingDockDoorProposal {
    return RollingDockDoorProposal(
        code = code,
        name = name,
        pinned = false,
        revision = 0,
        currency = "",
        startsAt = 0,
        status = RollingDockDoorProposalStatus.PLANNED,
    )
}
