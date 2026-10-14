package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default scorecard proposal is in its life cycle. */
enum class DefaultScorecardProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One default scorecard proposal, with what the planners need to act on it. */
data class DefaultScorecardProposal(
    var code: String,
    var title: String,
    var graceDays: Int,
    var timezone: String,
    var batchSize: Int,
    var locale: String,
    var attempts: Int,
    var windowDays: Int,
    var archived: Boolean,
    var status: DefaultScorecardProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultScorecardProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultScorecardProposalStatus): Boolean {
        when (status) {
            DefaultScorecardProposalStatus.IDLE -> {
                when (next) {
                    DefaultScorecardProposalStatus.STOPPED -> return true
                }
                return false
            }
            DefaultScorecardProposalStatus.WARMING -> {
                when (next) {
                    DefaultScorecardProposalStatus.READY, DefaultScorecardProposalStatus.DRAINING -> return true
                }
                return false
            }
            DefaultScorecardProposalStatus.READY -> return next == DefaultScorecardProposalStatus.DRAINING || next == DefaultScorecardProposalStatus.STOPPED
            DefaultScorecardProposalStatus.DRAINING -> {
                when (next) {
                    DefaultScorecardProposalStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultScorecardProposalStatus) {
        if (!canMoveTo(next)) {
            error("default scorecard proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round(attempts.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): DefaultScorecardProposal {
        return DefaultScorecardProposal(
            code = code,
            title = title,
            graceDays = graceDays,
            timezone = timezone,
            batchSize = batchSize,
            locale = locale,
            attempts = attempts,
            windowDays = windowDays,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default scorecard proposal in status idle. */
fun newDefaultScorecardProposal(code: String, title: String): DefaultScorecardProposal {
    return DefaultScorecardProposal(
        code = code,
        title = title,
        graceDays = 0,
        timezone = "",
        batchSize = 0,
        locale = "",
        attempts = 0,
        windowDays = 0,
        archived = false,
        status = DefaultScorecardProposalStatus.IDLE,
    )
}
