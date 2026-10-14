package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The carbon report agreement records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a carbon report agreement is in its life cycle. */
enum class CarbonReportAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A carbon report agreement as the carbon report team keeps it. */
data class CarbonReportAgreement(
    var code: String,
    var title: String,
    var floorValue: Double,
    var expiresAt: Int,
    var updatedAt: Int,
    var status: CarbonReportAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportAgreementStatus): Boolean {
        when (status) {
            CarbonReportAgreementStatus.NEW -> return next == CarbonReportAgreementStatus.RUNNING || next == CarbonReportAgreementStatus.FAILED
            CarbonReportAgreementStatus.QUEUED -> {
                when (next) {
                    CarbonReportAgreementStatus.RUNNING, CarbonReportAgreementStatus.FAILED -> return true
                }
                return false
            }
            CarbonReportAgreementStatus.RUNNING -> return next == CarbonReportAgreementStatus.DONE || next == CarbonReportAgreementStatus.FAILED
            CarbonReportAgreementStatus.DONE -> return next == CarbonReportAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CarbonReportAgreementStatus) {
        if (!canMoveTo(next)) {
            error("carbon report agreement $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"carbon report agreement"} $code"

    fun netVolume(): Double = round((updatedAt.toDouble() - floorValue) * 100) / 100

    fun snapshot(): CarbonReportAgreement {
        return CarbonReportAgreement(
            code = code,
            title = title,
            floorValue = floorValue,
            expiresAt = expiresAt,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a carbon report agreement in status new. */
fun newCarbonReportAgreement(code: String, title: String): CarbonReportAgreement {
    return CarbonReportAgreement(
        code = code,
        title = title,
        floorValue = 0.0,
        expiresAt = 0,
        updatedAt = 0,
        status = CarbonReportAgreementStatus.NEW,
    )
}
