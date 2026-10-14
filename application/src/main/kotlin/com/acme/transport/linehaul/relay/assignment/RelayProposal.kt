package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay proposal is in its life cycle. */
enum class RelayProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A relay proposal as the relay team keeps it. */
data class RelayProposal(
    var code: String,
    var label: String,
    var quantity: Int,
    var reviewer: String,
    var region: String,
    var windowDays: Int,
    var summary: String,
    var odometer: Int,
    var threshold: Double,
    var lane: String,
    var score: Double,
    var status: RelayProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayProposalStatus): Boolean {
        when (status) {
            RelayProposalStatus.OPEN -> {
                when (next) {
                    RelayProposalStatus.REOPENED -> return true
                }
                return false
            }
            RelayProposalStatus.IN_REVIEW -> {
                when (next) {
                    RelayProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            RelayProposalStatus.RESOLVED -> return next == RelayProposalStatus.REOPENED
            RelayProposalStatus.REOPENED -> {
                when (next) {
                    RelayProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayProposalStatus) {
        if (!canMoveTo(next)) {
            error("relay proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"relay proposal"} $code"

    fun weightedVolume(): Double = round((score - threshold) * 100) / 100

    fun snapshot(): RelayProposal {
        return RelayProposal(
            code = code,
            label = label,
            quantity = quantity,
            reviewer = reviewer,
            region = region,
            windowDays = windowDays,
            summary = summary,
            odometer = odometer,
            threshold = threshold,
            lane = lane,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a relay proposal in status open. */
fun newRelayProposal(code: String, label: String): RelayProposal {
    return RelayProposal(
        code = code,
        label = label,
        quantity = 0,
        reviewer = "",
        region = "",
        windowDays = 0,
        summary = "",
        odometer = 0,
        threshold = 0.0,
        lane = "",
        score = 0.0,
        status = RelayProposalStatus.OPEN,
    )
}
