package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive audit variance is in its life cycle. */
enum class AdaptiveAuditVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a adaptive audit variance and its current status. */
data class AdaptiveAuditVariance(
    var code: String,
    var title: String,
    var approved: Boolean,
    var region: String,
    var windowDays: Int,
    var name: String,
    var country: String,
    var updatedAt: Int,
    var status: AdaptiveAuditVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAuditVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAuditVarianceStatus): Boolean {
        when (status) {
            AdaptiveAuditVarianceStatus.OPEN -> return next == AdaptiveAuditVarianceStatus.REOPENED
            AdaptiveAuditVarianceStatus.IN_REVIEW -> return next == AdaptiveAuditVarianceStatus.REOPENED
            AdaptiveAuditVarianceStatus.RESOLVED -> return next == AdaptiveAuditVarianceStatus.REOPENED || next == AdaptiveAuditVarianceStatus.ARCHIVED
            AdaptiveAuditVarianceStatus.REOPENED -> return next == AdaptiveAuditVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveAuditVarianceStatus) {
        if (!canMoveTo(next)) {
            error("adaptive audit variance $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedTotal(): Double = round(windowDays.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): AdaptiveAuditVariance {
        return AdaptiveAuditVariance(
            code = code,
            title = title,
            approved = approved,
            region = region,
            windowDays = windowDays,
            name = name,
            country = country,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive audit variance in status open. */
fun newAdaptiveAuditVariance(code: String, title: String): AdaptiveAuditVariance {
    return AdaptiveAuditVariance(
        code = code,
        title = title,
        approved = false,
        region = "",
        windowDays = 0,
        name = "",
        country = "",
        updatedAt = 0,
        status = AdaptiveAuditVarianceStatus.OPEN,
    )
}
