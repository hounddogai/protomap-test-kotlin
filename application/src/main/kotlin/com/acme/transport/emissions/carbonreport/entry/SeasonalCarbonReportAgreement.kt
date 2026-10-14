package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.kit.text.normalize

/** Where a seasonal carbon report agreement is in its life cycle. */
enum class SeasonalCarbonReportAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal carbon report agreement. Its code is unique within a tenant. */
data class SeasonalCarbonReportAgreement(
    var code: String,
    var name: String,
    var currency: String,
    var attributes: MutableMap<String, String>,
    var fuelLiters: Double,
    var status: SeasonalCarbonReportAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCarbonReportAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCarbonReportAgreementStatus): Boolean {
        when (status) {
            SeasonalCarbonReportAgreementStatus.NEW -> return next == SeasonalCarbonReportAgreementStatus.FAILED
            SeasonalCarbonReportAgreementStatus.QUEUED -> {
                when (next) {
                    SeasonalCarbonReportAgreementStatus.RUNNING, SeasonalCarbonReportAgreementStatus.DONE -> return true
                }
                return false
            }
            SeasonalCarbonReportAgreementStatus.RUNNING -> {
                when (next) {
                    SeasonalCarbonReportAgreementStatus.DONE, SeasonalCarbonReportAgreementStatus.FAILED -> return true
                }
                return false
            }
            SeasonalCarbonReportAgreementStatus.DONE -> return next == SeasonalCarbonReportAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalCarbonReportAgreementStatus) {
        if (!canMoveTo(next)) {
            error("seasonal carbon report agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): SeasonalCarbonReportAgreement {
        return SeasonalCarbonReportAgreement(
            code = code,
            name = name,
            currency = currency,
            attributes = attributes,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal carbon report agreement in status new. */
fun newSeasonalCarbonReportAgreement(code: String, name: String): SeasonalCarbonReportAgreement {
    return SeasonalCarbonReportAgreement(
        code = code,
        name = name,
        currency = "",
        attributes = mutableMapOf<String, String>(),
        fuelLiters = 0.0,
        status = SeasonalCarbonReportAgreementStatus.NEW,
    )
}
