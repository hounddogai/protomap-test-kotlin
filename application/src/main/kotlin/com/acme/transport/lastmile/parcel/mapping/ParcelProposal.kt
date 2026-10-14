package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.text.normalize

/** Where a parcel proposal is in its life cycle. */
enum class ParcelProposalStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A parcel proposal. Its code is unique within a tenant. */
data class ParcelProposal(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var visible: Boolean,
    var routeCode: String,
    var currency: String,
    var phone: String,
    var source: String,
    var archived: Boolean,
    var limit: Int,
    var status: ParcelProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelProposalStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelProposalStatus): Boolean {
        when (status) {
            ParcelProposalStatus.DRAFT -> return next == ParcelProposalStatus.PENDING || next == ParcelProposalStatus.ACTIVE
            ParcelProposalStatus.PENDING -> return next == ParcelProposalStatus.CLOSED
            ParcelProposalStatus.ACTIVE -> return next == ParcelProposalStatus.CLOSED
            ParcelProposalStatus.SUSPENDED -> {
                when (next) {
                    ParcelProposalStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelProposalStatus) {
        if (!canMoveTo(next)) {
            error("parcel proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): ParcelProposal {
        return ParcelProposal(
            code = code,
            title = title,
            tags = tags,
            visible = visible,
            routeCode = routeCode,
            currency = currency,
            phone = phone,
            source = source,
            archived = archived,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a parcel proposal in status draft. */
fun newParcelProposal(code: String, title: String): ParcelProposal {
    return ParcelProposal(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        visible = false,
        routeCode = "",
        currency = "",
        phone = "",
        source = "",
        archived = false,
        limit = 0,
        status = ParcelProposalStatus.DRAFT,
    )
}
