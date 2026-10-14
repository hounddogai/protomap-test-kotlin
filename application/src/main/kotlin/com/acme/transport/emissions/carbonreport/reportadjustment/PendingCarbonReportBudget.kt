package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending carbon report budget is in its life cycle. */
enum class PendingCarbonReportBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending carbon report budget, with what the planners need to act on it. */
data class PendingCarbonReportBudget(
    var code: String,
    var title: String,
    var sequence: Int,
    var approved: Boolean,
    var durationMinutes: Int,
    var lane: String,
    var status: PendingCarbonReportBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCarbonReportBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCarbonReportBudgetStatus): Boolean {
        when (status) {
            PendingCarbonReportBudgetStatus.REQUESTED -> {
                when (next) {
                    PendingCarbonReportBudgetStatus.REVOKED, PendingCarbonReportBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            PendingCarbonReportBudgetStatus.GRANTED -> {
                when (next) {
                    PendingCarbonReportBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            PendingCarbonReportBudgetStatus.REVOKED -> return next == PendingCarbonReportBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingCarbonReportBudgetStatus) {
        if (!canMoveTo(next)) {
            error("pending carbon report budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((durationMinutes.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): PendingCarbonReportBudget {
        return PendingCarbonReportBudget(
            code = code,
            title = title,
            sequence = sequence,
            approved = approved,
            durationMinutes = durationMinutes,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending carbon report budget in status requested. */
fun newPendingCarbonReportBudget(code: String, title: String): PendingCarbonReportBudget {
    return PendingCarbonReportBudget(
        code = code,
        title = title,
        sequence = 0,
        approved = false,
        durationMinutes = 0,
        lane = "",
        status = PendingCarbonReportBudgetStatus.REQUESTED,
    )
}
