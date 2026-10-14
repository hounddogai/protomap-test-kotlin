package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial proposal is in its life cycle. */
enum class AccessorialProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A accessorial proposal. Its code is unique within a tenant. */
data class AccessorialProposal(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var phone: String,
    var revision: Int,
    var segment: String,
    var retries: Int,
    var cost: Double,
    var startsAt: Int,
    var status: AccessorialProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialProposalStatus): Boolean {
        when (status) {
            AccessorialProposalStatus.PROPOSED -> return next == AccessorialProposalStatus.APPROVED || next == AccessorialProposalStatus.WITHDRAWN
            AccessorialProposalStatus.APPROVED -> {
                when (next) {
                    AccessorialProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
            AccessorialProposalStatus.REJECTED -> return next == AccessorialProposalStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AccessorialProposalStatus) {
        if (!canMoveTo(next)) {
            error("accessorial proposal $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round(retries.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): AccessorialProposal {
        return AccessorialProposal(
            code = code,
            label = label,
            tags = tags,
            phone = phone,
            revision = revision,
            segment = segment,
            retries = retries,
            cost = cost,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a accessorial proposal in status proposed. */
fun newAccessorialProposal(code: String, label: String): AccessorialProposal {
    return AccessorialProposal(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        phone = "",
        revision = 0,
        segment = "",
        retries = 0,
        cost = 0.0,
        startsAt = 0,
        status = AccessorialProposalStatus.PROPOSED,
    )
}
