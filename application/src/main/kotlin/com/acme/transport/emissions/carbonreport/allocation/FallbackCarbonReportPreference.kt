package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback carbon report preference is in its life cycle. */
enum class FallbackCarbonReportPreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback carbon report preference. Its code is unique within a tenant. */
data class FallbackCarbonReportPreference(
    var code: String,
    var name: String,
    var active: Boolean,
    var contactEmail: String,
    var currency: String,
    var locale: String,
    var revision: Int,
    var sequence: Int,
    var odometer: Int,
    var status: FallbackCarbonReportPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCarbonReportPreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCarbonReportPreferenceStatus): Boolean {
        when (status) {
            FallbackCarbonReportPreferenceStatus.OPEN -> {
                when (next) {
                    FallbackCarbonReportPreferenceStatus.IN_REVIEW -> return true
                }
                return false
            }
            FallbackCarbonReportPreferenceStatus.IN_REVIEW -> {
                when (next) {
                    FallbackCarbonReportPreferenceStatus.REOPENED -> return true
                }
                return false
            }
            FallbackCarbonReportPreferenceStatus.RESOLVED -> return next == FallbackCarbonReportPreferenceStatus.REOPENED || next == FallbackCarbonReportPreferenceStatus.ARCHIVED
            FallbackCarbonReportPreferenceStatus.REOPENED -> return next == FallbackCarbonReportPreferenceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackCarbonReportPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("fallback carbon report preference $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun weightedExposure(): Double = round(odometer.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): FallbackCarbonReportPreference {
        return FallbackCarbonReportPreference(
            code = code,
            name = name,
            active = active,
            contactEmail = contactEmail,
            currency = currency,
            locale = locale,
            revision = revision,
            sequence = sequence,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback carbon report preference in status open. */
fun newFallbackCarbonReportPreference(code: String, name: String): FallbackCarbonReportPreference {
    return FallbackCarbonReportPreference(
        code = code,
        name = name,
        active = false,
        contactEmail = "",
        currency = "",
        locale = "",
        revision = 0,
        sequence = 0,
        odometer = 0,
        status = FallbackCarbonReportPreferenceStatus.OPEN,
    )
}
