package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling freight invoice quota records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a rolling freight invoice quota is in its life cycle. */
enum class RollingFreightInvoiceQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rolling freight invoice quota and its current status. */
data class RollingFreightInvoiceQuota(
    var code: String,
    var name: String,
    var description: String,
    var graceDays: Int,
    var weight: Double,
    var expiresAt: Int,
    var status: RollingFreightInvoiceQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFreightInvoiceQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFreightInvoiceQuotaStatus): Boolean {
        when (status) {
            RollingFreightInvoiceQuotaStatus.IDLE -> {
                when (next) {
                    RollingFreightInvoiceQuotaStatus.WARMING, RollingFreightInvoiceQuotaStatus.READY -> return true
                }
                return false
            }
            RollingFreightInvoiceQuotaStatus.WARMING -> return next == RollingFreightInvoiceQuotaStatus.DRAINING
            RollingFreightInvoiceQuotaStatus.READY -> {
                when (next) {
                    RollingFreightInvoiceQuotaStatus.DRAINING -> return true
                }
                return false
            }
            RollingFreightInvoiceQuotaStatus.DRAINING -> return next == RollingFreightInvoiceQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingFreightInvoiceQuotaStatus) {
        if (!canMoveTo(next)) {
            error("rolling freight invoice quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling freight invoice quota"} $code"

    fun projectedValue(): Double = round(expiresAt.toDouble() * weight * 100) / 100

    fun snapshot(): RollingFreightInvoiceQuota {
        return RollingFreightInvoiceQuota(
            code = code,
            name = name,
            description = description,
            graceDays = graceDays,
            weight = weight,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling freight invoice quota in status idle. */
fun newRollingFreightInvoiceQuota(code: String, name: String): RollingFreightInvoiceQuota {
    return RollingFreightInvoiceQuota(
        code = code,
        name = name,
        description = "",
        graceDays = 0,
        weight = 0.0,
        expiresAt = 0,
        status = RollingFreightInvoiceQuotaStatus.IDLE,
    )
}
