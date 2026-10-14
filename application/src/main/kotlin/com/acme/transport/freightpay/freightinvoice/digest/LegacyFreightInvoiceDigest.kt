package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy freight invoice digest records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a legacy freight invoice digest is in its life cycle. */
enum class LegacyFreightInvoiceDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A legacy freight invoice digest as the freight invoice team keeps it. */
data class LegacyFreightInvoiceDigest(
    var code: String,
    var name: String,
    var ratio: Double,
    var capacity: Int,
    var createdAt: Int,
    var ceiling: Double,
    var country: String,
    var timezone: String,
    var status: LegacyFreightInvoiceDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyFreightInvoiceDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyFreightInvoiceDigestStatus): Boolean {
        when (status) {
            LegacyFreightInvoiceDigestStatus.REQUESTED -> return next == LegacyFreightInvoiceDigestStatus.GRANTED || next == LegacyFreightInvoiceDigestStatus.REVOKED
            LegacyFreightInvoiceDigestStatus.GRANTED -> {
                when (next) {
                    LegacyFreightInvoiceDigestStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyFreightInvoiceDigestStatus.REVOKED -> {
                when (next) {
                    LegacyFreightInvoiceDigestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyFreightInvoiceDigestStatus) {
        if (!canMoveTo(next)) {
            error("legacy freight invoice digest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((ceiling + capacity.toDouble()) * 100) / 100

    fun snapshot(): LegacyFreightInvoiceDigest {
        return LegacyFreightInvoiceDigest(
            code = code,
            name = name,
            ratio = ratio,
            capacity = capacity,
            createdAt = createdAt,
            ceiling = ceiling,
            country = country,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy freight invoice digest in status requested. */
fun newLegacyFreightInvoiceDigest(code: String, name: String): LegacyFreightInvoiceDigest {
    return LegacyFreightInvoiceDigest(
        code = code,
        name = name,
        ratio = 0.0,
        capacity = 0,
        createdAt = 0,
        ceiling = 0.0,
        country = "",
        timezone = "",
        status = LegacyFreightInvoiceDigestStatus.REQUESTED,
    )
}
