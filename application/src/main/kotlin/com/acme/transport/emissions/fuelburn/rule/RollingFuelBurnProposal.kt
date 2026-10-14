package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling fuel burn proposal is in its life cycle. */
enum class RollingFuelBurnProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rolling fuel burn proposal and its current status. */
data class RollingFuelBurnProposal(
    var code: String,
    var name: String,
    var title: String,
    var discount: Double,
    var quantity: Int,
    var batchSize: Int,
    var reviewer: String,
    var sequence: Int,
    var floorValue: Double,
    var trailerId: String,
    var score: Double,
    var status: RollingFuelBurnProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelBurnProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelBurnProposalStatus): Boolean {
        when (status) {
            RollingFuelBurnProposalStatus.IDLE -> return next == RollingFuelBurnProposalStatus.DRAINING
            RollingFuelBurnProposalStatus.WARMING -> {
                when (next) {
                    RollingFuelBurnProposalStatus.DRAINING -> return true
                }
                return false
            }
            RollingFuelBurnProposalStatus.READY -> return next == RollingFuelBurnProposalStatus.DRAINING || next == RollingFuelBurnProposalStatus.STOPPED
            RollingFuelBurnProposalStatus.DRAINING -> return next == RollingFuelBurnProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingFuelBurnProposalStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel burn proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round((score + floorValue) * 100) / 100

    fun snapshot(): RollingFuelBurnProposal {
        return RollingFuelBurnProposal(
            code = code,
            name = name,
            title = title,
            discount = discount,
            quantity = quantity,
            batchSize = batchSize,
            reviewer = reviewer,
            sequence = sequence,
            floorValue = floorValue,
            trailerId = trailerId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling fuel burn proposal in status idle. */
fun newRollingFuelBurnProposal(code: String, name: String): RollingFuelBurnProposal {
    return RollingFuelBurnProposal(
        code = code,
        name = name,
        title = "",
        discount = 0.0,
        quantity = 0,
        batchSize = 0,
        reviewer = "",
        sequence = 0,
        floorValue = 0.0,
        trailerId = "",
        score = 0.0,
        status = RollingFuelBurnProposalStatus.IDLE,
    )
}
