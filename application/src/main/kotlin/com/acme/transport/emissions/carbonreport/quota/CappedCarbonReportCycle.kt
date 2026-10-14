package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped carbon report cycle is in its life cycle. */
enum class CappedCarbonReportCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped carbon report cycle as the carbon report team keeps it. */
data class CappedCarbonReportCycle(
    var code: String,
    var name: String,
    var odometer: Int,
    var ceiling: Double,
    var label: String,
    var contactEmail: String,
    var retries: Int,
    var stopCount: Int,
    var status: CappedCarbonReportCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCarbonReportCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCarbonReportCycleStatus): Boolean {
        when (status) {
            CappedCarbonReportCycleStatus.PROPOSED -> return next == CappedCarbonReportCycleStatus.APPROVED || next == CappedCarbonReportCycleStatus.REJECTED
            CappedCarbonReportCycleStatus.APPROVED -> return next == CappedCarbonReportCycleStatus.REJECTED || next == CappedCarbonReportCycleStatus.WITHDRAWN
            CappedCarbonReportCycleStatus.REJECTED -> {
                when (next) {
                    CappedCarbonReportCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCarbonReportCycleStatus) {
        if (!canMoveTo(next)) {
            error("capped carbon report cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun effectiveTotal(): Double = round(retries.toDouble() * ceiling * 100) / 100

    fun snapshot(): CappedCarbonReportCycle {
        return CappedCarbonReportCycle(
            code = code,
            name = name,
            odometer = odometer,
            ceiling = ceiling,
            label = label,
            contactEmail = contactEmail,
            retries = retries,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped carbon report cycle in status proposed. */
fun newCappedCarbonReportCycle(code: String, name: String): CappedCarbonReportCycle {
    return CappedCarbonReportCycle(
        code = code,
        name = name,
        odometer = 0,
        ceiling = 0.0,
        label = "",
        contactEmail = "",
        retries = 0,
        stopCount = 0,
        status = CappedCarbonReportCycleStatus.PROPOSED,
    )
}
