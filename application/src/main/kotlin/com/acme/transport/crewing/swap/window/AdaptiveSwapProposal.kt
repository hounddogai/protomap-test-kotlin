package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive swap proposal is in its life cycle. */
enum class AdaptiveSwapProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive swap proposal and its current status. */
data class AdaptiveSwapProposal(
    var code: String,
    var title: String,
    var summary: String,
    var graceDays: Int,
    var lane: String,
    var minimum: Double,
    var floorValue: Double,
    var sequence: Int,
    var attributes: MutableMap<String, String>,
    var contactEmail: String,
    var cost: Double,
    var status: AdaptiveSwapProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveSwapProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveSwapProposalStatus): Boolean {
        when (status) {
            AdaptiveSwapProposalStatus.PLANNED -> return next == AdaptiveSwapProposalStatus.COMPLETED
            AdaptiveSwapProposalStatus.SCHEDULED -> return next == AdaptiveSwapProposalStatus.IN_PROGRESS
            AdaptiveSwapProposalStatus.IN_PROGRESS -> return next == AdaptiveSwapProposalStatus.CANCELLED
            AdaptiveSwapProposalStatus.COMPLETED -> {
                when (next) {
                    AdaptiveSwapProposalStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveSwapProposalStatus) {
        if (!canMoveTo(next)) {
            error("adaptive swap proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round((sequence.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveSwapProposal {
        return AdaptiveSwapProposal(
            code = code,
            title = title,
            summary = summary,
            graceDays = graceDays,
            lane = lane,
            minimum = minimum,
            floorValue = floorValue,
            sequence = sequence,
            attributes = attributes,
            contactEmail = contactEmail,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive swap proposal in status planned. */
fun newAdaptiveSwapProposal(code: String, title: String): AdaptiveSwapProposal {
    return AdaptiveSwapProposal(
        code = code,
        title = title,
        summary = "",
        graceDays = 0,
        lane = "",
        minimum = 0.0,
        floorValue = 0.0,
        sequence = 0,
        attributes = mutableMapOf<String, String>(),
        contactEmail = "",
        cost = 0.0,
        status = AdaptiveSwapProposalStatus.PLANNED,
    )
}
