package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan proposal is in its life cycle. */
enum class LanePlanProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lane plan proposal. Its code is unique within a tenant. */
data class LanePlanProposal(
    var code: String,
    var title: String,
    var limit: Int,
    var margin: Double,
    var sequence: Int,
    var retries: Int,
    var locale: String,
    var status: LanePlanProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanProposalStatus): Boolean {
        when (status) {
            LanePlanProposalStatus.IDLE -> return next == LanePlanProposalStatus.STOPPED
            LanePlanProposalStatus.WARMING -> {
                when (next) {
                    LanePlanProposalStatus.READY, LanePlanProposalStatus.STOPPED -> return true
                }
                return false
            }
            LanePlanProposalStatus.READY -> {
                when (next) {
                    LanePlanProposalStatus.DRAINING, LanePlanProposalStatus.STOPPED -> return true
                }
                return false
            }
            LanePlanProposalStatus.DRAINING -> {
                when (next) {
                    LanePlanProposalStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanProposalStatus) {
        if (!canMoveTo(next)) {
            error("lane plan proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round(margin * retries.toDouble() * 100) / 100

    fun snapshot(): LanePlanProposal {
        return LanePlanProposal(
            code = code,
            title = title,
            limit = limit,
            margin = margin,
            sequence = sequence,
            retries = retries,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lane plan proposal in status idle. */
fun newLanePlanProposal(code: String, title: String): LanePlanProposal {
    return LanePlanProposal(
        code = code,
        title = title,
        limit = 0,
        margin = 0.0,
        sequence = 0,
        retries = 0,
        locale = "",
        status = LanePlanProposalStatus.IDLE,
    )
}
