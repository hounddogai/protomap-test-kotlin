package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted fuel surcharge proposal is in its life cycle. */
enum class WeightedFuelSurchargeProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted fuel surcharge proposal. Its code is unique within a tenant. */
data class WeightedFuelSurchargeProposal(
    var code: String,
    var name: String,
    var routeCode: String,
    var contactEmail: String,
    var retries: Int,
    var quantity: Int,
    var status: WeightedFuelSurchargeProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelSurchargeProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelSurchargeProposalStatus): Boolean {
        when (status) {
            WeightedFuelSurchargeProposalStatus.REQUESTED -> {
                when (next) {
                    WeightedFuelSurchargeProposalStatus.GRANTED -> return true
                }
                return false
            }
            WeightedFuelSurchargeProposalStatus.GRANTED -> {
                when (next) {
                    WeightedFuelSurchargeProposalStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedFuelSurchargeProposalStatus.REVOKED -> return next == WeightedFuelSurchargeProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: WeightedFuelSurchargeProposalStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel surcharge proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round(quantity.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): WeightedFuelSurchargeProposal {
        return WeightedFuelSurchargeProposal(
            code = code,
            name = name,
            routeCode = routeCode,
            contactEmail = contactEmail,
            retries = retries,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted fuel surcharge proposal in status requested. */
fun newWeightedFuelSurchargeProposal(code: String, name: String): WeightedFuelSurchargeProposal {
    return WeightedFuelSurchargeProposal(
        code = code,
        name = name,
        routeCode = "",
        contactEmail = "",
        retries = 0,
        quantity = 0,
        status = WeightedFuelSurchargeProposalStatus.REQUESTED,
    )
}
