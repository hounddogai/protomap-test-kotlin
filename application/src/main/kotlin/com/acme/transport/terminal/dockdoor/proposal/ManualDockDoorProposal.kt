package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual dock door proposal records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a manual dock door proposal is in its life cycle. */
enum class ManualDockDoorProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a manual dock door proposal and its current status. */
data class ManualDockDoorProposal(
    var code: String,
    var title: String,
    var sequence: Int,
    var attributes: MutableMap<String, String>,
    var dock: String,
    var batchSize: Int,
    var active: Boolean,
    var currency: String,
    var budget: Double,
    var status: ManualDockDoorProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDockDoorProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDockDoorProposalStatus): Boolean {
        when (status) {
            ManualDockDoorProposalStatus.OPEN -> return next == ManualDockDoorProposalStatus.REOPENED || next == ManualDockDoorProposalStatus.ARCHIVED
            ManualDockDoorProposalStatus.IN_REVIEW -> return next == ManualDockDoorProposalStatus.ARCHIVED
            ManualDockDoorProposalStatus.RESOLVED -> return next == ManualDockDoorProposalStatus.REOPENED || next == ManualDockDoorProposalStatus.ARCHIVED
            ManualDockDoorProposalStatus.REOPENED -> {
                when (next) {
                    ManualDockDoorProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDockDoorProposalStatus) {
        if (!canMoveTo(next)) {
            error("manual dock door proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual dock door proposal"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((budget + batchSize.toDouble()) * 100) / 100

    fun snapshot(): ManualDockDoorProposal {
        return ManualDockDoorProposal(
            code = code,
            title = title,
            sequence = sequence,
            attributes = attributes,
            dock = dock,
            batchSize = batchSize,
            active = active,
            currency = currency,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual dock door proposal in status open. */
fun newManualDockDoorProposal(code: String, title: String): ManualDockDoorProposal {
    return ManualDockDoorProposal(
        code = code,
        title = title,
        sequence = 0,
        attributes = mutableMapOf<String, String>(),
        dock = "",
        batchSize = 0,
        active = false,
        currency = "",
        budget = 0.0,
        status = ManualDockDoorProposalStatus.OPEN,
    )
}
