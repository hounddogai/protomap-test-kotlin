package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional carbon report adjustment records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a regional carbon report adjustment is in its life cycle. */
enum class RegionalCarbonReportAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional carbon report adjustment. Its code is unique within a tenant. */
data class RegionalCarbonReportAdjustment(
    var code: String,
    var name: String,
    var cost: Double,
    var quantity: Int,
    var version: Int,
    var status: RegionalCarbonReportAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCarbonReportAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCarbonReportAdjustmentStatus): Boolean {
        when (status) {
            RegionalCarbonReportAdjustmentStatus.NEW -> return next == RegionalCarbonReportAdjustmentStatus.FAILED
            RegionalCarbonReportAdjustmentStatus.QUEUED -> return next == RegionalCarbonReportAdjustmentStatus.RUNNING || next == RegionalCarbonReportAdjustmentStatus.DONE
            RegionalCarbonReportAdjustmentStatus.RUNNING -> return next == RegionalCarbonReportAdjustmentStatus.DONE || next == RegionalCarbonReportAdjustmentStatus.FAILED
            RegionalCarbonReportAdjustmentStatus.DONE -> {
                when (next) {
                    RegionalCarbonReportAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCarbonReportAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("regional carbon report adjustment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"regional carbon report adjustment"} $code"

    fun projectedValue(): Double = round((quantity.toDouble() + cost) * 100) / 100

    fun snapshot(): RegionalCarbonReportAdjustment {
        return RegionalCarbonReportAdjustment(
            code = code,
            name = name,
            cost = cost,
            quantity = quantity,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional carbon report adjustment in status new. */
fun newRegionalCarbonReportAdjustment(code: String, name: String): RegionalCarbonReportAdjustment {
    return RegionalCarbonReportAdjustment(
        code = code,
        name = name,
        cost = 0.0,
        quantity = 0,
        version = 0,
        status = RegionalCarbonReportAdjustmentStatus.NEW,
    )
}
