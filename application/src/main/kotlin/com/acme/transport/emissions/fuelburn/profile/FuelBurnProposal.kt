package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.kit.text.normalize

/** Where a fuel burn proposal is in its life cycle. */
enum class FuelBurnProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fuel burn proposal and its current status. */
data class FuelBurnProposal(
    var code: String,
    var name: String,
    var vehicleId: String,
    var shiftCode: String,
    var rate: Double,
    var status: FuelBurnProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnProposalStatus): Boolean {
        when (status) {
            FuelBurnProposalStatus.IDLE -> return next == FuelBurnProposalStatus.STOPPED
            FuelBurnProposalStatus.WARMING -> return next == FuelBurnProposalStatus.READY || next == FuelBurnProposalStatus.STOPPED
            FuelBurnProposalStatus.READY -> {
                when (next) {
                    FuelBurnProposalStatus.DRAINING, FuelBurnProposalStatus.STOPPED -> return true
                }
                return false
            }
            FuelBurnProposalStatus.DRAINING -> return next == FuelBurnProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FuelBurnProposalStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = rate

    fun snapshot(): FuelBurnProposal {
        return FuelBurnProposal(
            code = code,
            name = name,
            vehicleId = vehicleId,
            shiftCode = shiftCode,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel burn proposal in status idle. */
fun newFuelBurnProposal(code: String, name: String): FuelBurnProposal {
    return FuelBurnProposal(
        code = code,
        name = name,
        vehicleId = "",
        shiftCode = "",
        rate = 0.0,
        status = FuelBurnProposalStatus.IDLE,
    )
}
