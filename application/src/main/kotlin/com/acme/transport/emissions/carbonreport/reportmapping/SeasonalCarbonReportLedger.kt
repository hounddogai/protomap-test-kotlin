package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal carbon report ledger is in its life cycle. */
enum class SeasonalCarbonReportLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal carbon report ledger as the carbon report team keeps it. */
data class SeasonalCarbonReportLedger(
    var code: String,
    var label: String,
    var reviewer: String,
    var timezone: String,
    var priority: Int,
    var contactEmail: String,
    var floorValue: Double,
    var version: Int,
    var discount: Double,
    var ceiling: Double,
    var status: SeasonalCarbonReportLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCarbonReportLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCarbonReportLedgerStatus): Boolean {
        when (status) {
            SeasonalCarbonReportLedgerStatus.REQUESTED -> return next == SeasonalCarbonReportLedgerStatus.REVOKED || next == SeasonalCarbonReportLedgerStatus.EXPIRED
            SeasonalCarbonReportLedgerStatus.GRANTED -> return next == SeasonalCarbonReportLedgerStatus.EXPIRED
            SeasonalCarbonReportLedgerStatus.REVOKED -> return next == SeasonalCarbonReportLedgerStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalCarbonReportLedgerStatus) {
        if (!canMoveTo(next)) {
            error("seasonal carbon report ledger $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"seasonal carbon report ledger"} $code"

    fun adjustedValue(): Double = round((priority.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): SeasonalCarbonReportLedger {
        return SeasonalCarbonReportLedger(
            code = code,
            label = label,
            reviewer = reviewer,
            timezone = timezone,
            priority = priority,
            contactEmail = contactEmail,
            floorValue = floorValue,
            version = version,
            discount = discount,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal carbon report ledger in status requested. */
fun newSeasonalCarbonReportLedger(code: String, label: String): SeasonalCarbonReportLedger {
    return SeasonalCarbonReportLedger(
        code = code,
        label = label,
        reviewer = "",
        timezone = "",
        priority = 0,
        contactEmail = "",
        floorValue = 0.0,
        version = 0,
        discount = 0.0,
        ceiling = 0.0,
        status = SeasonalCarbonReportLedgerStatus.REQUESTED,
    )
}
