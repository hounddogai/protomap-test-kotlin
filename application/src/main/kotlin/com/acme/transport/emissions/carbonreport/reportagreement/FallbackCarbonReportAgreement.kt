package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback carbon report agreement records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a fallback carbon report agreement is in its life cycle. */
enum class FallbackCarbonReportAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fallback carbon report agreement and its current status. */
data class FallbackCarbonReportAgreement(
    var code: String,
    var title: String,
    var budget: Double,
    var ratio: Double,
    var rate: Double,
    var expiresAt: Int,
    var retries: Int,
    var status: FallbackCarbonReportAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCarbonReportAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCarbonReportAgreementStatus): Boolean {
        when (status) {
            FallbackCarbonReportAgreementStatus.CREATED -> {
                when (next) {
                    FallbackCarbonReportAgreementStatus.VALIDATED, FallbackCarbonReportAgreementStatus.RETIRED -> return true
                }
                return false
            }
            FallbackCarbonReportAgreementStatus.VALIDATED -> {
                when (next) {
                    FallbackCarbonReportAgreementStatus.PUBLISHED, FallbackCarbonReportAgreementStatus.RETIRED -> return true
                }
                return false
            }
            FallbackCarbonReportAgreementStatus.PUBLISHED -> return next == FallbackCarbonReportAgreementStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackCarbonReportAgreementStatus) {
        if (!canMoveTo(next)) {
            error("fallback carbon report agreement $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round((ratio + budget) * 100) / 100

    fun snapshot(): FallbackCarbonReportAgreement {
        return FallbackCarbonReportAgreement(
            code = code,
            title = title,
            budget = budget,
            ratio = ratio,
            rate = rate,
            expiresAt = expiresAt,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback carbon report agreement in status created. */
fun newFallbackCarbonReportAgreement(code: String, title: String): FallbackCarbonReportAgreement {
    return FallbackCarbonReportAgreement(
        code = code,
        title = title,
        budget = 0.0,
        ratio = 0.0,
        rate = 0.0,
        expiresAt = 0,
        retries = 0,
        status = FallbackCarbonReportAgreementStatus.CREATED,
    )
}
