package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient duty period proposal is in its life cycle. */
enum class LenientDutyPeriodProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lenient duty period proposal, with what the planners need to act on it. */
data class LenientDutyPeriodProposal(
    var code: String,
    var name: String,
    var country: String,
    var tags: MutableList<String>,
    var endsAt: Int,
    var attributes: MutableMap<String, String>,
    var category: String,
    var retries: Int,
    var status: LenientDutyPeriodProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDutyPeriodProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDutyPeriodProposalStatus): Boolean {
        when (status) {
            LenientDutyPeriodProposalStatus.PLANNED -> {
                when (next) {
                    LenientDutyPeriodProposalStatus.SCHEDULED, LenientDutyPeriodProposalStatus.COMPLETED -> return true
                }
                return false
            }
            LenientDutyPeriodProposalStatus.SCHEDULED -> return next == LenientDutyPeriodProposalStatus.COMPLETED || next == LenientDutyPeriodProposalStatus.CANCELLED
            LenientDutyPeriodProposalStatus.IN_PROGRESS -> {
                when (next) {
                    LenientDutyPeriodProposalStatus.COMPLETED -> return true
                }
                return false
            }
            LenientDutyPeriodProposalStatus.COMPLETED -> return next == LenientDutyPeriodProposalStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientDutyPeriodProposalStatus) {
        if (!canMoveTo(next)) {
            error("lenient duty period proposal $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient duty period proposal"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round(endsAt.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): LenientDutyPeriodProposal {
        return LenientDutyPeriodProposal(
            code = code,
            name = name,
            country = country,
            tags = tags,
            endsAt = endsAt,
            attributes = attributes,
            category = category,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient duty period proposal in status planned. */
fun newLenientDutyPeriodProposal(code: String, name: String): LenientDutyPeriodProposal {
    return LenientDutyPeriodProposal(
        code = code,
        name = name,
        country = "",
        tags = mutableListOf<String>(),
        endsAt = 0,
        attributes = mutableMapOf<String, String>(),
        category = "",
        retries = 0,
        status = LenientDutyPeriodProposalStatus.PLANNED,
    )
}
