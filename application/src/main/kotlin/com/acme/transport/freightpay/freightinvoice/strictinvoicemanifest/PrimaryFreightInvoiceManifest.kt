package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary freight invoice manifest records of freightpay's freight invoice work, and the rules, stores,
// and calculations around them.

/** Where a primary freight invoice manifest is in its life cycle. */
enum class PrimaryFreightInvoiceManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a primary freight invoice manifest and its current status. */
data class PrimaryFreightInvoiceManifest(
    var code: String,
    var name: String,
    var graceDays: Int,
    var revision: Int,
    var ratio: Double,
    var title: String,
    var weight: Double,
    var endsAt: Int,
    var status: PrimaryFreightInvoiceManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFreightInvoiceManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFreightInvoiceManifestStatus): Boolean {
        when (status) {
            PrimaryFreightInvoiceManifestStatus.IDLE -> {
                when (next) {
                    PrimaryFreightInvoiceManifestStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryFreightInvoiceManifestStatus.WARMING -> return next == PrimaryFreightInvoiceManifestStatus.READY || next == PrimaryFreightInvoiceManifestStatus.STOPPED
            PrimaryFreightInvoiceManifestStatus.READY -> return next == PrimaryFreightInvoiceManifestStatus.STOPPED
            PrimaryFreightInvoiceManifestStatus.DRAINING -> return next == PrimaryFreightInvoiceManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryFreightInvoiceManifestStatus) {
        if (!canMoveTo(next)) {
            error("primary freight invoice manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((weight + endsAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryFreightInvoiceManifest {
        return PrimaryFreightInvoiceManifest(
            code = code,
            name = name,
            graceDays = graceDays,
            revision = revision,
            ratio = ratio,
            title = title,
            weight = weight,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary freight invoice manifest in status idle. */
fun newPrimaryFreightInvoiceManifest(code: String, name: String): PrimaryFreightInvoiceManifest {
    return PrimaryFreightInvoiceManifest(
        code = code,
        name = name,
        graceDays = 0,
        revision = 0,
        ratio = 0.0,
        title = "",
        weight = 0.0,
        endsAt = 0,
        status = PrimaryFreightInvoiceManifestStatus.IDLE,
    )
}
