package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal tender proposal records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a seasonal tender proposal is in its life cycle. */
enum class SeasonalTenderProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal tender proposal. Its code is unique within a tenant. */
data class SeasonalTenderProposal(
    var code: String,
    var label: String,
    var description: String,
    var etaMinutes: Int,
    var unit: String,
    var driverId: String,
    var expiresAt: Int,
    var ceiling: Double,
    var category: String,
    var routeCode: String,
    var rate: Double,
    var status: SeasonalTenderProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTenderProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTenderProposalStatus): Boolean {
        when (status) {
            SeasonalTenderProposalStatus.PLANNED -> return next == SeasonalTenderProposalStatus.SCHEDULED || next == SeasonalTenderProposalStatus.COMPLETED
            SeasonalTenderProposalStatus.SCHEDULED -> return next == SeasonalTenderProposalStatus.COMPLETED || next == SeasonalTenderProposalStatus.CANCELLED
            SeasonalTenderProposalStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalTenderProposalStatus.COMPLETED, SeasonalTenderProposalStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalTenderProposalStatus.COMPLETED -> return next == SeasonalTenderProposalStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalTenderProposalStatus) {
        if (!canMoveTo(next)) {
            error("seasonal tender proposal $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedVolume(): Double = round((etaMinutes.toDouble() - ceiling) * 100) / 100

    fun snapshot(): SeasonalTenderProposal {
        return SeasonalTenderProposal(
            code = code,
            label = label,
            description = description,
            etaMinutes = etaMinutes,
            unit = unit,
            driverId = driverId,
            expiresAt = expiresAt,
            ceiling = ceiling,
            category = category,
            routeCode = routeCode,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a seasonal tender proposal in status planned. */
fun newSeasonalTenderProposal(code: String, label: String): SeasonalTenderProposal {
    return SeasonalTenderProposal(
        code = code,
        label = label,
        description = "",
        etaMinutes = 0,
        unit = "",
        driverId = "",
        expiresAt = 0,
        ceiling = 0.0,
        category = "",
        routeCode = "",
        rate = 0.0,
        status = SeasonalTenderProposalStatus.PLANNED,
    )
}
