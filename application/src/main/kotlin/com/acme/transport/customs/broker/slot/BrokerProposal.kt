package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker proposal is in its life cycle. */
enum class BrokerProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A broker proposal. Its code is unique within a tenant. */
data class BrokerProposal(
    var code: String,
    var title: String,
    var category: String,
    var endsAt: Int,
    var contactEmail: String,
    var ratio: Double,
    var currency: String,
    var pallets: Int,
    var status: BrokerProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerProposalStatus): Boolean {
        when (status) {
            BrokerProposalStatus.OPEN -> return next == BrokerProposalStatus.REOPENED || next == BrokerProposalStatus.ARCHIVED
            BrokerProposalStatus.IN_REVIEW -> return next == BrokerProposalStatus.RESOLVED || next == BrokerProposalStatus.REOPENED
            BrokerProposalStatus.RESOLVED -> {
                when (next) {
                    BrokerProposalStatus.REOPENED -> return true
                }
                return false
            }
            BrokerProposalStatus.REOPENED -> {
                when (next) {
                    BrokerProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerProposalStatus) {
        if (!canMoveTo(next)) {
            error("broker proposal $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netExposure(): Double = round((ratio + endsAt.toDouble()) * 100) / 100

    fun snapshot(): BrokerProposal {
        return BrokerProposal(
            code = code,
            title = title,
            category = category,
            endsAt = endsAt,
            contactEmail = contactEmail,
            ratio = ratio,
            currency = currency,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a broker proposal in status open. */
fun newBrokerProposal(code: String, title: String): BrokerProposal {
    return BrokerProposal(
        code = code,
        title = title,
        category = "",
        endsAt = 0,
        contactEmail = "",
        ratio = 0.0,
        currency = "",
        pallets = 0,
        status = BrokerProposalStatus.OPEN,
    )
}
