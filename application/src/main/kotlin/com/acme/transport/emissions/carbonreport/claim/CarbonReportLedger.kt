package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report ledger is in its life cycle. */
enum class CarbonReportLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A carbon report ledger. Its code is unique within a tenant. */
data class CarbonReportLedger(
    var code: String,
    var label: String,
    var externalRef: String,
    var distanceKm: Double,
    var attempts: Int,
    var shiftCode: String,
    var margin: Double,
    var region: String,
    var description: String,
    var status: CarbonReportLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportLedgerStatus): Boolean {
        when (status) {
            CarbonReportLedgerStatus.PROPOSED -> return next == CarbonReportLedgerStatus.REJECTED || next == CarbonReportLedgerStatus.WITHDRAWN
            CarbonReportLedgerStatus.APPROVED -> {
                when (next) {
                    CarbonReportLedgerStatus.REJECTED, CarbonReportLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
            CarbonReportLedgerStatus.REJECTED -> return next == CarbonReportLedgerStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CarbonReportLedgerStatus) {
        if (!canMoveTo(next)) {
            error("carbon report ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedLoad(): Double = round((attempts.toDouble() + margin) * 100) / 100

    fun snapshot(): CarbonReportLedger {
        return CarbonReportLedger(
            code = code,
            label = label,
            externalRef = externalRef,
            distanceKm = distanceKm,
            attempts = attempts,
            shiftCode = shiftCode,
            margin = margin,
            region = region,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a carbon report ledger in status proposed. */
fun newCarbonReportLedger(code: String, label: String): CarbonReportLedger {
    return CarbonReportLedger(
        code = code,
        label = label,
        externalRef = "",
        distanceKm = 0.0,
        attempts = 0,
        shiftCode = "",
        margin = 0.0,
        region = "",
        description = "",
        status = CarbonReportLedgerStatus.PROPOSED,
    )
}
