package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel surcharge proposal records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge proposal is in its life cycle. */
enum class FuelSurchargeProposalStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fuel surcharge proposal and its current status. */
data class FuelSurchargeProposal(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var pallets: Int,
    var shiftCode: String,
    var status: FuelSurchargeProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeProposalStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeProposalStatus): Boolean {
        when (status) {
            FuelSurchargeProposalStatus.OPEN -> {
                when (next) {
                    FuelSurchargeProposalStatus.RESOLVED, FuelSurchargeProposalStatus.REOPENED -> return true
                }
                return false
            }
            FuelSurchargeProposalStatus.IN_REVIEW -> {
                when (next) {
                    FuelSurchargeProposalStatus.REOPENED, FuelSurchargeProposalStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelSurchargeProposalStatus.RESOLVED -> return next == FuelSurchargeProposalStatus.REOPENED || next == FuelSurchargeProposalStatus.ARCHIVED
            FuelSurchargeProposalStatus.REOPENED -> {
                when (next) {
                    FuelSurchargeProposalStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeProposalStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round((pallets.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): FuelSurchargeProposal {
        return FuelSurchargeProposal(
            code = code,
            label = label,
            expiresAt = expiresAt,
            pallets = pallets,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel surcharge proposal in status open. */
fun newFuelSurchargeProposal(code: String, label: String): FuelSurchargeProposal {
    return FuelSurchargeProposal(
        code = code,
        label = label,
        expiresAt = 0,
        pallets = 0,
        shiftCode = "",
        status = FuelSurchargeProposalStatus.OPEN,
    )
}
