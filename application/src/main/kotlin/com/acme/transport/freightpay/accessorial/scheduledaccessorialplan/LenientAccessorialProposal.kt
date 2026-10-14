package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.text.normalize

/** Where a lenient accessorial proposal is in its life cycle. */
enum class LenientAccessorialProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient accessorial proposal. Its code is unique within a tenant. */
data class LenientAccessorialProposal(
    var code: String,
    var name: String,
    var locale: String,
    var archived: Boolean,
    var phone: String,
    var updatedAt: Int,
    var status: LenientAccessorialProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAccessorialProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAccessorialProposalStatus): Boolean {
        when (status) {
            LenientAccessorialProposalStatus.DRAFT -> {
                when (next) {
                    LenientAccessorialProposalStatus.SUSPENDED, LenientAccessorialProposalStatus.CLOSED -> return true
                }
                return false
            }
            LenientAccessorialProposalStatus.PENDING -> return next == LenientAccessorialProposalStatus.ACTIVE || next == LenientAccessorialProposalStatus.SUSPENDED
            LenientAccessorialProposalStatus.ACTIVE -> {
                when (next) {
                    LenientAccessorialProposalStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientAccessorialProposalStatus.SUSPENDED -> return next == LenientAccessorialProposalStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientAccessorialProposalStatus) {
        if (!canMoveTo(next)) {
            error("lenient accessorial proposal $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): LenientAccessorialProposal {
        return LenientAccessorialProposal(
            code = code,
            name = name,
            locale = locale,
            archived = archived,
            phone = phone,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lenient accessorial proposal in status draft. */
fun newLenientAccessorialProposal(code: String, name: String): LenientAccessorialProposal {
    return LenientAccessorialProposal(
        code = code,
        name = name,
        locale = "",
        archived = false,
        phone = "",
        updatedAt = 0,
        status = LenientAccessorialProposalStatus.DRAFT,
    )
}
