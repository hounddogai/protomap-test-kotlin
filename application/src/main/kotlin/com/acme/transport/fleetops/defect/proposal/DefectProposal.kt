package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect proposal records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect proposal is in its life cycle. */
enum class DefectProposalStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a defect proposal and its current status. */
data class DefectProposal(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var maximum: Double,
    var fuelLiters: Double,
    var category: String,
    var priority: Int,
    var distanceKm: Double,
    var status: DefectProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectProposalStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectProposalStatus): Boolean {
        when (status) {
            DefectProposalStatus.IDLE -> return next == DefectProposalStatus.STOPPED
            DefectProposalStatus.WARMING -> return next == DefectProposalStatus.DRAINING || next == DefectProposalStatus.STOPPED
            DefectProposalStatus.READY -> {
                when (next) {
                    DefectProposalStatus.DRAINING, DefectProposalStatus.STOPPED -> return true
                }
                return false
            }
            DefectProposalStatus.DRAINING -> return next == DefectProposalStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefectProposalStatus) {
        if (!canMoveTo(next)) {
            error("defect proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((maximum - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefectProposal {
        return DefectProposal(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            maximum = maximum,
            fuelLiters = fuelLiters,
            category = category,
            priority = priority,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a defect proposal in status idle. */
fun newDefectProposal(code: String, name: String): DefectProposal {
    return DefectProposal(
        code = code,
        name = name,
        durationMinutes = 0,
        maximum = 0.0,
        fuelLiters = 0.0,
        category = "",
        priority = 0,
        distanceKm = 0.0,
        status = DefectProposalStatus.IDLE,
    )
}
