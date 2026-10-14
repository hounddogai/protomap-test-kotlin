package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive carbon report score records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a adaptive carbon report score is in its life cycle. */
enum class AdaptiveCarbonReportScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive carbon report score. Its code is unique within a tenant. */
data class AdaptiveCarbonReportScore(
    var code: String,
    var name: String,
    var description: String,
    var title: String,
    var contactEmail: String,
    var externalRef: String,
    var budget: Double,
    var amount: Double,
    var status: AdaptiveCarbonReportScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCarbonReportScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCarbonReportScoreStatus): Boolean {
        when (status) {
            AdaptiveCarbonReportScoreStatus.REQUESTED -> return next == AdaptiveCarbonReportScoreStatus.GRANTED || next == AdaptiveCarbonReportScoreStatus.REVOKED
            AdaptiveCarbonReportScoreStatus.GRANTED -> {
                when (next) {
                    AdaptiveCarbonReportScoreStatus.REVOKED, AdaptiveCarbonReportScoreStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveCarbonReportScoreStatus.REVOKED -> {
                when (next) {
                    AdaptiveCarbonReportScoreStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveCarbonReportScoreStatus) {
        if (!canMoveTo(next)) {
            error("adaptive carbon report score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive carbon report score"} $code"

    fun effectiveTotal(): Double = round((budget + amount) * 100) / 100

    fun snapshot(): AdaptiveCarbonReportScore {
        return AdaptiveCarbonReportScore(
            code = code,
            name = name,
            description = description,
            title = title,
            contactEmail = contactEmail,
            externalRef = externalRef,
            budget = budget,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive carbon report score in status requested. */
fun newAdaptiveCarbonReportScore(code: String, name: String): AdaptiveCarbonReportScore {
    return AdaptiveCarbonReportScore(
        code = code,
        name = name,
        description = "",
        title = "",
        contactEmail = "",
        externalRef = "",
        budget = 0.0,
        amount = 0.0,
        status = AdaptiveCarbonReportScoreStatus.REQUESTED,
    )
}
