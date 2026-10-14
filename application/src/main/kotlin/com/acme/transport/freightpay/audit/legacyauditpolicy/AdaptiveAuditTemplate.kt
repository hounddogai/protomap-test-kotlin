package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive audit template is in its life cycle. */
enum class AdaptiveAuditTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive audit template. Its code is unique within a tenant. */
data class AdaptiveAuditTemplate(
    var code: String,
    var title: String,
    var unit: String,
    var minimum: Double,
    var depot: String,
    var driverId: String,
    var locale: String,
    var windowDays: Int,
    var status: AdaptiveAuditTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAuditTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAuditTemplateStatus): Boolean {
        when (status) {
            AdaptiveAuditTemplateStatus.PROPOSED -> return next == AdaptiveAuditTemplateStatus.REJECTED || next == AdaptiveAuditTemplateStatus.WITHDRAWN
            AdaptiveAuditTemplateStatus.APPROVED -> {
                when (next) {
                    AdaptiveAuditTemplateStatus.REJECTED, AdaptiveAuditTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveAuditTemplateStatus.REJECTED -> return next == AdaptiveAuditTemplateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveAuditTemplateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive audit template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netValue(): Double = round((windowDays.toDouble() - minimum) * 100) / 100

    fun snapshot(): AdaptiveAuditTemplate {
        return AdaptiveAuditTemplate(
            code = code,
            title = title,
            unit = unit,
            minimum = minimum,
            depot = depot,
            driverId = driverId,
            locale = locale,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive audit template in status proposed. */
fun newAdaptiveAuditTemplate(code: String, title: String): AdaptiveAuditTemplate {
    return AdaptiveAuditTemplate(
        code = code,
        title = title,
        unit = "",
        minimum = 0.0,
        depot = "",
        driverId = "",
        locale = "",
        windowDays = 0,
        status = AdaptiveAuditTemplateStatus.PROPOSED,
    )
}
