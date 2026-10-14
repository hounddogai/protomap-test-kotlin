package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.kit.text.normalize

/** Where a primary carbon report template is in its life cycle. */
enum class PrimaryCarbonReportTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary carbon report template. Its code is unique within a tenant. */
data class PrimaryCarbonReportTemplate(
    var code: String,
    var title: String,
    var visible: Boolean,
    var discount: Double,
    var locale: String,
    var summary: String,
    var status: PrimaryCarbonReportTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCarbonReportTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCarbonReportTemplateStatus): Boolean {
        when (status) {
            PrimaryCarbonReportTemplateStatus.OPEN -> return next == PrimaryCarbonReportTemplateStatus.REOPENED || next == PrimaryCarbonReportTemplateStatus.ARCHIVED
            PrimaryCarbonReportTemplateStatus.IN_REVIEW -> return next == PrimaryCarbonReportTemplateStatus.RESOLVED || next == PrimaryCarbonReportTemplateStatus.REOPENED
            PrimaryCarbonReportTemplateStatus.RESOLVED -> {
                when (next) {
                    PrimaryCarbonReportTemplateStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryCarbonReportTemplateStatus.REOPENED -> {
                when (next) {
                    PrimaryCarbonReportTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryCarbonReportTemplateStatus) {
        if (!canMoveTo(next)) {
            error("primary carbon report template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary carbon report template"} $code"

    fun measuredValue(): Double = discount

    fun snapshot(): PrimaryCarbonReportTemplate {
        return PrimaryCarbonReportTemplate(
            code = code,
            title = title,
            visible = visible,
            discount = discount,
            locale = locale,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary carbon report template in status open. */
fun newPrimaryCarbonReportTemplate(code: String, title: String): PrimaryCarbonReportTemplate {
    return PrimaryCarbonReportTemplate(
        code = code,
        title = title,
        visible = false,
        discount = 0.0,
        locale = "",
        summary = "",
        status = PrimaryCarbonReportTemplateStatus.OPEN,
    )
}
