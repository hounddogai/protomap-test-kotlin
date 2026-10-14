package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration proposal is in its life cycle. */
enum class DeclarationProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A declaration proposal as the declaration team keeps it. */
data class DeclarationProposal(
    var code: String,
    var name: String,
    var vehicleId: String,
    var distanceKm: Double,
    var pallets: Int,
    var status: DeclarationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationProposalStatus): Boolean {
        when (status) {
            DeclarationProposalStatus.IDLE -> {
                when (next) {
                    DeclarationProposalStatus.WARMING -> return true
                }
                return false
            }
            DeclarationProposalStatus.WARMING -> {
                when (next) {
                    DeclarationProposalStatus.READY, DeclarationProposalStatus.STOPPED -> return true
                }
                return false
            }
            DeclarationProposalStatus.READY -> return next == DeclarationProposalStatus.DRAINING || next == DeclarationProposalStatus.STOPPED
            DeclarationProposalStatus.DRAINING -> {
                when (next) {
                    DeclarationProposalStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationProposalStatus) {
        if (!canMoveTo(next)) {
            error("declaration proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"declaration proposal"} $code"

    fun effectiveTotal(): Double = round((pallets.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): DeclarationProposal {
        return DeclarationProposal(
            code = code,
            name = name,
            vehicleId = vehicleId,
            distanceKm = distanceKm,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a declaration proposal in status idle. */
fun newDeclarationProposal(code: String, name: String): DeclarationProposal {
    return DeclarationProposal(
        code = code,
        name = name,
        vehicleId = "",
        distanceKm = 0.0,
        pallets = 0,
        status = DeclarationProposalStatus.IDLE,
    )
}
