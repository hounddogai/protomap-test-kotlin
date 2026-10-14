package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice manifest records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice manifest is in its life cycle. */
enum class FreightInvoiceManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A freight invoice manifest as the freight invoice team keeps it. */
data class FreightInvoiceManifest(
    var code: String,
    var title: String,
    var windowDays: Int,
    var minimum: Double,
    var externalRef: String,
    var endsAt: Int,
    var startsAt: Int,
    var floorValue: Double,
    var ceiling: Double,
    var status: FreightInvoiceManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceManifestStatus): Boolean {
        when (status) {
            FreightInvoiceManifestStatus.DRAFT -> return next == FreightInvoiceManifestStatus.CLOSED
            FreightInvoiceManifestStatus.PENDING -> return next == FreightInvoiceManifestStatus.SUSPENDED
            FreightInvoiceManifestStatus.ACTIVE -> return next == FreightInvoiceManifestStatus.CLOSED
            FreightInvoiceManifestStatus.SUSPENDED -> return next == FreightInvoiceManifestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceManifestStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedVolume(): Double = round((windowDays.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceManifest {
        return FreightInvoiceManifest(
            code = code,
            title = title,
            windowDays = windowDays,
            minimum = minimum,
            externalRef = externalRef,
            endsAt = endsAt,
            startsAt = startsAt,
            floorValue = floorValue,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a freight invoice manifest in status draft. */
fun newFreightInvoiceManifest(code: String, title: String): FreightInvoiceManifest {
    return FreightInvoiceManifest(
        code = code,
        title = title,
        windowDays = 0,
        minimum = 0.0,
        externalRef = "",
        endsAt = 0,
        startsAt = 0,
        floorValue = 0.0,
        ceiling = 0.0,
        status = FreightInvoiceManifestStatus.DRAFT,
    )
}
