package com.acme.transport.emissions.carbonreport.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The carbon report template records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a carbon report template is in its life cycle. */
enum class CarbonReportTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One carbon report template, with what the planners need to act on it. */
data class CarbonReportTemplate(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var startsAt: Int,
    var odometer: Int,
    var revision: Int,
    var ownerId: String,
    var externalRef: String,
    var amount: Double,
    var approved: Boolean,
    var status: CarbonReportTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportTemplateStatus): Boolean {
        when (status) {
            CarbonReportTemplateStatus.PLANNED -> {
                when (next) {
                    CarbonReportTemplateStatus.COMPLETED, CarbonReportTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            CarbonReportTemplateStatus.SCHEDULED -> return next == CarbonReportTemplateStatus.COMPLETED || next == CarbonReportTemplateStatus.CANCELLED
            CarbonReportTemplateStatus.IN_PROGRESS -> return next == CarbonReportTemplateStatus.CANCELLED
            CarbonReportTemplateStatus.COMPLETED -> return next == CarbonReportTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CarbonReportTemplateStatus) {
        if (!canMoveTo(next)) {
            error("carbon report template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"carbon report template"} $code"

    fun effectiveLoad(): Double = round(startsAt.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): CarbonReportTemplate {
        return CarbonReportTemplate(
            code = code,
            title = title,
            expiresAt = expiresAt,
            startsAt = startsAt,
            odometer = odometer,
            revision = revision,
            ownerId = ownerId,
            externalRef = externalRef,
            amount = amount,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a carbon report template in status planned. */
fun newCarbonReportTemplate(code: String, title: String): CarbonReportTemplate {
    return CarbonReportTemplate(
        code = code,
        title = title,
        expiresAt = 0,
        startsAt = 0,
        odometer = 0,
        revision = 0,
        ownerId = "",
        externalRef = "",
        amount = 0.0,
        approved = false,
        status = CarbonReportTemplateStatus.PLANNED,
    )
}
