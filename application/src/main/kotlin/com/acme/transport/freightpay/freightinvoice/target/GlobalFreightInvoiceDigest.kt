package com.acme.transport.freightpay.freightinvoice.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global freight invoice digest is in its life cycle. */
enum class GlobalFreightInvoiceDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global freight invoice digest as the freight invoice team keeps it. */
data class GlobalFreightInvoiceDigest(
    var code: String,
    var name: String,
    var attempts: Int,
    var batchSize: Int,
    var pallets: Int,
    var archived: Boolean,
    var active: Boolean,
    var trailerId: String,
    var externalRef: String,
    var durationMinutes: Int,
    var visible: Boolean,
    var status: GlobalFreightInvoiceDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFreightInvoiceDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFreightInvoiceDigestStatus): Boolean {
        when (status) {
            GlobalFreightInvoiceDigestStatus.REQUESTED -> return next == GlobalFreightInvoiceDigestStatus.GRANTED || next == GlobalFreightInvoiceDigestStatus.REVOKED
            GlobalFreightInvoiceDigestStatus.GRANTED -> {
                when (next) {
                    GlobalFreightInvoiceDigestStatus.REVOKED -> return true
                }
                return false
            }
            GlobalFreightInvoiceDigestStatus.REVOKED -> {
                when (next) {
                    GlobalFreightInvoiceDigestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalFreightInvoiceDigestStatus) {
        if (!canMoveTo(next)) {
            error("global freight invoice digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((pallets.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): GlobalFreightInvoiceDigest {
        return GlobalFreightInvoiceDigest(
            code = code,
            name = name,
            attempts = attempts,
            batchSize = batchSize,
            pallets = pallets,
            archived = archived,
            active = active,
            trailerId = trailerId,
            externalRef = externalRef,
            durationMinutes = durationMinutes,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global freight invoice digest in status requested. */
fun newGlobalFreightInvoiceDigest(code: String, name: String): GlobalFreightInvoiceDigest {
    return GlobalFreightInvoiceDigest(
        code = code,
        name = name,
        attempts = 0,
        batchSize = 0,
        pallets = 0,
        archived = false,
        active = false,
        trailerId = "",
        externalRef = "",
        durationMinutes = 0,
        visible = false,
        status = GlobalFreightInvoiceDigestStatus.REQUESTED,
    )
}
