package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional qualification proposal records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a regional qualification proposal is in its life cycle. */
enum class RegionalQualificationProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional qualification proposal. Its code is unique within a tenant. */
data class RegionalQualificationProposal(
    var code: String,
    var title: String,
    var quantity: Int,
    var pallets: Int,
    var floorValue: Double,
    var retries: Int,
    var status: RegionalQualificationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalQualificationProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalQualificationProposalStatus): Boolean {
        when (status) {
            RegionalQualificationProposalStatus.REQUESTED -> return next == RegionalQualificationProposalStatus.REVOKED || next == RegionalQualificationProposalStatus.EXPIRED
            RegionalQualificationProposalStatus.GRANTED -> return next == RegionalQualificationProposalStatus.REVOKED || next == RegionalQualificationProposalStatus.EXPIRED
            RegionalQualificationProposalStatus.REVOKED -> return next == RegionalQualificationProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalQualificationProposalStatus) {
        if (!canMoveTo(next)) {
            error("regional qualification proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round(pallets.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): RegionalQualificationProposal {
        return RegionalQualificationProposal(
            code = code,
            title = title,
            quantity = quantity,
            pallets = pallets,
            floorValue = floorValue,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional qualification proposal in status requested. */
fun newRegionalQualificationProposal(code: String, title: String): RegionalQualificationProposal {
    return RegionalQualificationProposal(
        code = code,
        title = title,
        quantity = 0,
        pallets = 0,
        floorValue = 0.0,
        retries = 0,
        status = RegionalQualificationProposalStatus.REQUESTED,
    )
}
