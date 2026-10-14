package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy hub proposal is in its life cycle. */
enum class LegacyHubProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a legacy hub proposal and its current status. */
data class LegacyHubProposal(
    var code: String,
    var label: String,
    var archived: Boolean,
    var channel: String,
    var updatedAt: Int,
    var ceiling: Double,
    var priority: Int,
    var timezone: String,
    var description: String,
    var source: String,
    var routeCode: String,
    var status: LegacyHubProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyHubProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyHubProposalStatus): Boolean {
        when (status) {
            LegacyHubProposalStatus.PLANNED -> return next == LegacyHubProposalStatus.SCHEDULED
            LegacyHubProposalStatus.SCHEDULED -> return next == LegacyHubProposalStatus.CANCELLED
            LegacyHubProposalStatus.IN_PROGRESS -> return next == LegacyHubProposalStatus.COMPLETED
            LegacyHubProposalStatus.COMPLETED -> return next == LegacyHubProposalStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyHubProposalStatus) {
        if (!canMoveTo(next)) {
            error("legacy hub proposal $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round((updatedAt.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): LegacyHubProposal {
        return LegacyHubProposal(
            code = code,
            label = label,
            archived = archived,
            channel = channel,
            updatedAt = updatedAt,
            ceiling = ceiling,
            priority = priority,
            timezone = timezone,
            description = description,
            source = source,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy hub proposal in status planned. */
fun newLegacyHubProposal(code: String, label: String): LegacyHubProposal {
    return LegacyHubProposal(
        code = code,
        label = label,
        archived = false,
        channel = "",
        updatedAt = 0,
        ceiling = 0.0,
        priority = 0,
        timezone = "",
        description = "",
        source = "",
        routeCode = "",
        status = LegacyHubProposalStatus.PLANNED,
    )
}
