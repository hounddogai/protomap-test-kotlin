package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report tier is in its life cycle. */
enum class CarbonReportTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A carbon report tier. Its code is unique within a tenant. */
data class CarbonReportTier(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var carrier: String,
    var category: String,
    var notes: String,
    var rate: Double,
    var status: CarbonReportTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportTierStatus): Boolean {
        when (status) {
            CarbonReportTierStatus.NEW -> return next == CarbonReportTierStatus.QUEUED || next == CarbonReportTierStatus.FAILED
            CarbonReportTierStatus.QUEUED -> return next == CarbonReportTierStatus.DONE || next == CarbonReportTierStatus.FAILED
            CarbonReportTierStatus.RUNNING -> {
                when (next) {
                    CarbonReportTierStatus.FAILED -> return true
                }
                return false
            }
            CarbonReportTierStatus.DONE -> {
                when (next) {
                    CarbonReportTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportTierStatus) {
        if (!canMoveTo(next)) {
            error("carbon report tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"carbon report tier"} $code"

    fun weightedLoad(): Double = round(rate * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): CarbonReportTier {
        return CarbonReportTier(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            carrier = carrier,
            category = category,
            notes = notes,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a carbon report tier in status new. */
fun newCarbonReportTier(code: String, label: String): CarbonReportTier {
    return CarbonReportTier(
        code = code,
        label = label,
        durationMinutes = 0,
        carrier = "",
        category = "",
        notes = "",
        rate = 0.0,
        status = CarbonReportTierStatus.NEW,
    )
}
