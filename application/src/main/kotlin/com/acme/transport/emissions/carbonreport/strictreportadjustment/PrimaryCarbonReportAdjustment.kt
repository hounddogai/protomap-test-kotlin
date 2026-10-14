package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary carbon report adjustment records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a primary carbon report adjustment is in its life cycle. */
enum class PrimaryCarbonReportAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary carbon report adjustment as the carbon report team keeps it. */
data class PrimaryCarbonReportAdjustment(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var minimum: Double,
    var category: String,
    var updatedAt: Int,
    var status: PrimaryCarbonReportAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCarbonReportAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCarbonReportAdjustmentStatus): Boolean {
        when (status) {
            PrimaryCarbonReportAdjustmentStatus.REQUESTED -> return next == PrimaryCarbonReportAdjustmentStatus.GRANTED
            PrimaryCarbonReportAdjustmentStatus.GRANTED -> return next == PrimaryCarbonReportAdjustmentStatus.EXPIRED
            PrimaryCarbonReportAdjustmentStatus.REVOKED -> {
                when (next) {
                    PrimaryCarbonReportAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryCarbonReportAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("primary carbon report adjustment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"primary carbon report adjustment"} $code"

    fun adjustedExposure(): Double = round(updatedAt.toDouble() * minimum * 100) / 100

    fun snapshot(): PrimaryCarbonReportAdjustment {
        return PrimaryCarbonReportAdjustment(
            code = code,
            title = title,
            expiresAt = expiresAt,
            minimum = minimum,
            category = category,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary carbon report adjustment in status requested. */
fun newPrimaryCarbonReportAdjustment(code: String, title: String): PrimaryCarbonReportAdjustment {
    return PrimaryCarbonReportAdjustment(
        code = code,
        title = title,
        expiresAt = 0,
        minimum = 0.0,
        category = "",
        updatedAt = 0,
        status = PrimaryCarbonReportAdjustmentStatus.REQUESTED,
    )
}
