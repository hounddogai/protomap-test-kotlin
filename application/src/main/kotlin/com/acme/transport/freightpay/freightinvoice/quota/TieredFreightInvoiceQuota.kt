package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered freight invoice quota records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a tiered freight invoice quota is in its life cycle. */
enum class TieredFreightInvoiceQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One tiered freight invoice quota, with what the planners need to act on it. */
data class TieredFreightInvoiceQuota(
    var code: String,
    var name: String,
    var currency: String,
    var amount: Double,
    var label: String,
    var startsAt: Int,
    var status: TieredFreightInvoiceQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFreightInvoiceQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFreightInvoiceQuotaStatus): Boolean {
        when (status) {
            TieredFreightInvoiceQuotaStatus.REQUESTED -> {
                when (next) {
                    TieredFreightInvoiceQuotaStatus.GRANTED, TieredFreightInvoiceQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            TieredFreightInvoiceQuotaStatus.GRANTED -> {
                when (next) {
                    TieredFreightInvoiceQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            TieredFreightInvoiceQuotaStatus.REVOKED -> return next == TieredFreightInvoiceQuotaStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredFreightInvoiceQuotaStatus) {
        if (!canMoveTo(next)) {
            error("tiered freight invoice quota $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $name"

    fun netTotal(): Double = round((amount + startsAt.toDouble()) * 100) / 100

    fun snapshot(): TieredFreightInvoiceQuota {
        return TieredFreightInvoiceQuota(
            code = code,
            name = name,
            currency = currency,
            amount = amount,
            label = label,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered freight invoice quota in status requested. */
fun newTieredFreightInvoiceQuota(code: String, name: String): TieredFreightInvoiceQuota {
    return TieredFreightInvoiceQuota(
        code = code,
        name = name,
        currency = "",
        amount = 0.0,
        label = "",
        startsAt = 0,
        status = TieredFreightInvoiceQuotaStatus.REQUESTED,
    )
}
