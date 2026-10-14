package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling defect proposal records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a rolling defect proposal is in its life cycle. */
enum class RollingDefectProposalStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a rolling defect proposal and its current status. */
data class RollingDefectProposal(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var stopCount: Int,
    var attempts: Int,
    var routeCode: String,
    var externalRef: String,
    var phone: String,
    var amount: Double,
    var approved: Boolean,
    var vehicleId: String,
    var status: RollingDefectProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDefectProposalStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDefectProposalStatus): Boolean {
        when (status) {
            RollingDefectProposalStatus.PROPOSED -> return next == RollingDefectProposalStatus.APPROVED || next == RollingDefectProposalStatus.REJECTED
            RollingDefectProposalStatus.APPROVED -> {
                when (next) {
                    RollingDefectProposalStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingDefectProposalStatus.REJECTED -> return next == RollingDefectProposalStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingDefectProposalStatus) {
        if (!canMoveTo(next)) {
            error("rolling defect proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling defect proposal"} $code"

    fun weightedTotal(): Double = round((distanceKm + stopCount.toDouble()) * 100) / 100

    fun snapshot(): RollingDefectProposal {
        return RollingDefectProposal(
            code = code,
            name = name,
            distanceKm = distanceKm,
            stopCount = stopCount,
            attempts = attempts,
            routeCode = routeCode,
            externalRef = externalRef,
            phone = phone,
            amount = amount,
            approved = approved,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling defect proposal in status proposed. */
fun newRollingDefectProposal(code: String, name: String): RollingDefectProposal {
    return RollingDefectProposal(
        code = code,
        name = name,
        distanceKm = 0.0,
        stopCount = 0,
        attempts = 0,
        routeCode = "",
        externalRef = "",
        phone = "",
        amount = 0.0,
        approved = false,
        vehicleId = "",
        status = RollingDefectProposalStatus.PROPOSED,
    )
}
