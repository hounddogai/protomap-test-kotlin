package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual utilization proposal records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a manual utilization proposal is in its life cycle. */
enum class ManualUtilizationProposalStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a manual utilization proposal and its current status. */
data class ManualUtilizationProposal(
    var code: String,
    var name: String,
    var minimum: Double,
    var sequence: Int,
    var quantity: Int,
    var source: String,
    var phone: String,
    var budget: Double,
    var createdAt: Int,
    var vehicleId: String,
    var amount: Double,
    var status: ManualUtilizationProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualUtilizationProposalStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualUtilizationProposalStatus): Boolean {
        when (status) {
            ManualUtilizationProposalStatus.REQUESTED -> {
                when (next) {
                    ManualUtilizationProposalStatus.REVOKED -> return true
                }
                return false
            }
            ManualUtilizationProposalStatus.GRANTED -> {
                when (next) {
                    ManualUtilizationProposalStatus.EXPIRED -> return true
                }
                return false
            }
            ManualUtilizationProposalStatus.REVOKED -> return next == ManualUtilizationProposalStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ManualUtilizationProposalStatus) {
        if (!canMoveTo(next)) {
            error("manual utilization proposal $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveTotal(): Double = round((sequence.toDouble() + amount) * 100) / 100

    fun snapshot(): ManualUtilizationProposal {
        return ManualUtilizationProposal(
            code = code,
            name = name,
            minimum = minimum,
            sequence = sequence,
            quantity = quantity,
            source = source,
            phone = phone,
            budget = budget,
            createdAt = createdAt,
            vehicleId = vehicleId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual utilization proposal in status requested. */
fun newManualUtilizationProposal(code: String, name: String): ManualUtilizationProposal {
    return ManualUtilizationProposal(
        code = code,
        name = name,
        minimum = 0.0,
        sequence = 0,
        quantity = 0,
        source = "",
        phone = "",
        budget = 0.0,
        createdAt = 0,
        vehicleId = "",
        amount = 0.0,
        status = ManualUtilizationProposalStatus.REQUESTED,
    )
}
