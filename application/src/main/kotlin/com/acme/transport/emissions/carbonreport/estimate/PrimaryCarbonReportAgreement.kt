package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary carbon report agreement is in its life cycle. */
enum class PrimaryCarbonReportAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary carbon report agreement, with what the planners need to act on it. */
data class PrimaryCarbonReportAgreement(
    var code: String,
    var label: String,
    var ratio: Double,
    var currency: String,
    var odometer: Int,
    var depot: String,
    var floorValue: Double,
    var description: String,
    var graceDays: Int,
    var fuelLiters: Double,
    var status: PrimaryCarbonReportAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCarbonReportAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCarbonReportAgreementStatus): Boolean {
        when (status) {
            PrimaryCarbonReportAgreementStatus.OPEN -> {
                when (next) {
                    PrimaryCarbonReportAgreementStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryCarbonReportAgreementStatus.IN_REVIEW -> return next == PrimaryCarbonReportAgreementStatus.RESOLVED || next == PrimaryCarbonReportAgreementStatus.ARCHIVED
            PrimaryCarbonReportAgreementStatus.RESOLVED -> {
                when (next) {
                    PrimaryCarbonReportAgreementStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryCarbonReportAgreementStatus.REOPENED -> return next == PrimaryCarbonReportAgreementStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryCarbonReportAgreementStatus) {
        if (!canMoveTo(next)) {
            error("primary carbon report agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round((floorValue + graceDays.toDouble()) * 100) / 100

    fun snapshot(): PrimaryCarbonReportAgreement {
        return PrimaryCarbonReportAgreement(
            code = code,
            label = label,
            ratio = ratio,
            currency = currency,
            odometer = odometer,
            depot = depot,
            floorValue = floorValue,
            description = description,
            graceDays = graceDays,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a primary carbon report agreement in status open. */
fun newPrimaryCarbonReportAgreement(code: String, label: String): PrimaryCarbonReportAgreement {
    return PrimaryCarbonReportAgreement(
        code = code,
        label = label,
        ratio = 0.0,
        currency = "",
        odometer = 0,
        depot = "",
        floorValue = 0.0,
        description = "",
        graceDays = 0,
        fuelLiters = 0.0,
        status = PrimaryCarbonReportAgreementStatus.OPEN,
    )
}
