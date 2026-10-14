package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit quota is in its life cycle. */
enum class AuditQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A audit quota as the audit team keeps it. */
data class AuditQuota(
    var code: String,
    var label: String,
    var odometer: Int,
    var threshold: Double,
    var graceDays: Int,
    var rate: Double,
    var pallets: Int,
    var title: String,
    var cost: Double,
    var channel: String,
    var status: AuditQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditQuotaStatus): Boolean {
        when (status) {
            AuditQuotaStatus.OPEN -> {
                when (next) {
                    AuditQuotaStatus.RESOLVED, AuditQuotaStatus.REOPENED -> return true
                }
                return false
            }
            AuditQuotaStatus.IN_REVIEW -> return next == AuditQuotaStatus.ARCHIVED
            AuditQuotaStatus.RESOLVED -> return next == AuditQuotaStatus.REOPENED
            AuditQuotaStatus.REOPENED -> {
                when (next) {
                    AuditQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditQuotaStatus) {
        if (!canMoveTo(next)) {
            error("audit quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round(graceDays.toDouble() * threshold * 100) / 100

    fun snapshot(): AuditQuota {
        return AuditQuota(
            code = code,
            label = label,
            odometer = odometer,
            threshold = threshold,
            graceDays = graceDays,
            rate = rate,
            pallets = pallets,
            title = title,
            cost = cost,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a audit quota in status open. */
fun newAuditQuota(code: String, label: String): AuditQuota {
    return AuditQuota(
        code = code,
        label = label,
        odometer = 0,
        threshold = 0.0,
        graceDays = 0,
        rate = 0.0,
        pallets = 0,
        title = "",
        cost = 0.0,
        channel = "",
        status = AuditQuotaStatus.OPEN,
    )
}
