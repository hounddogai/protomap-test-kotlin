package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback freight invoice manifest records of freightpay's freight invoice work, and the rules, stores,
// and calculations around them.

/** Where a fallback freight invoice manifest is in its life cycle. */
enum class FallbackFreightInvoiceManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback freight invoice manifest as the freight invoice team keeps it. */
data class FallbackFreightInvoiceManifest(
    var code: String,
    var title: String,
    var notes: String,
    var active: Boolean,
    var fuelLiters: Double,
    var durationMinutes: Int,
    var source: String,
    var checksum: String,
    var amount: Double,
    var margin: Double,
    var budget: Double,
    var status: FallbackFreightInvoiceManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackFreightInvoiceManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackFreightInvoiceManifestStatus): Boolean {
        when (status) {
            FallbackFreightInvoiceManifestStatus.NEW -> return next == FallbackFreightInvoiceManifestStatus.QUEUED || next == FallbackFreightInvoiceManifestStatus.FAILED
            FallbackFreightInvoiceManifestStatus.QUEUED -> return next == FallbackFreightInvoiceManifestStatus.DONE || next == FallbackFreightInvoiceManifestStatus.FAILED
            FallbackFreightInvoiceManifestStatus.RUNNING -> {
                when (next) {
                    FallbackFreightInvoiceManifestStatus.DONE -> return true
                }
                return false
            }
            FallbackFreightInvoiceManifestStatus.DONE -> {
                when (next) {
                    FallbackFreightInvoiceManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackFreightInvoiceManifestStatus) {
        if (!canMoveTo(next)) {
            error("fallback freight invoice manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedTotal(): Double = round(margin * fuelLiters * 100) / 100

    fun snapshot(): FallbackFreightInvoiceManifest {
        return FallbackFreightInvoiceManifest(
            code = code,
            title = title,
            notes = notes,
            active = active,
            fuelLiters = fuelLiters,
            durationMinutes = durationMinutes,
            source = source,
            checksum = checksum,
            amount = amount,
            margin = margin,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fallback freight invoice manifest in status new. */
fun newFallbackFreightInvoiceManifest(code: String, title: String): FallbackFreightInvoiceManifest {
    return FallbackFreightInvoiceManifest(
        code = code,
        title = title,
        notes = "",
        active = false,
        fuelLiters = 0.0,
        durationMinutes = 0,
        source = "",
        checksum = "",
        amount = 0.0,
        margin = 0.0,
        budget = 0.0,
        status = FallbackFreightInvoiceManifestStatus.NEW,
    )
}
