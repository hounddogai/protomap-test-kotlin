package com.acme.transport.emissions.carbonreport.template

import com.acme.transport.kit.text.normalize

/** Where a carbon report cycle is in its life cycle. */
enum class CarbonReportCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a carbon report cycle and its current status. */
data class CarbonReportCycle(
    var code: String,
    var label: String,
    var currency: String,
    var driverId: String,
    var depot: String,
    var tags: MutableList<String>,
    var amount: Double,
    var status: CarbonReportCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportCycleStatus): Boolean {
        when (status) {
            CarbonReportCycleStatus.REQUESTED -> return next == CarbonReportCycleStatus.EXPIRED
            CarbonReportCycleStatus.GRANTED -> {
                when (next) {
                    CarbonReportCycleStatus.REVOKED -> return true
                }
                return false
            }
            CarbonReportCycleStatus.REVOKED -> return next == CarbonReportCycleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CarbonReportCycleStatus) {
        if (!canMoveTo(next)) {
            error("carbon report cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = amount

    fun snapshot(): CarbonReportCycle {
        return CarbonReportCycle(
            code = code,
            label = label,
            currency = currency,
            driverId = driverId,
            depot = depot,
            tags = tags,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a carbon report cycle in status requested. */
fun newCarbonReportCycle(code: String, label: String): CarbonReportCycle {
    return CarbonReportCycle(
        code = code,
        label = label,
        currency = "",
        driverId = "",
        depot = "",
        tags = mutableListOf<String>(),
        amount = 0.0,
        status = CarbonReportCycleStatus.REQUESTED,
    )
}
