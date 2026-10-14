package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal freight invoice manifest is in its life cycle. */
enum class SeasonalFreightInvoiceManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal freight invoice manifest as the freight invoice team keeps it. */
data class SeasonalFreightInvoiceManifest(
    var code: String,
    var label: String,
    var margin: Double,
    var limit: Int,
    var floorValue: Double,
    var category: String,
    var threshold: Double,
    var attempts: Int,
    var country: String,
    var status: SeasonalFreightInvoiceManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalFreightInvoiceManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalFreightInvoiceManifestStatus): Boolean {
        when (status) {
            SeasonalFreightInvoiceManifestStatus.DRAFT -> return next == SeasonalFreightInvoiceManifestStatus.PENDING || next == SeasonalFreightInvoiceManifestStatus.ACTIVE
            SeasonalFreightInvoiceManifestStatus.PENDING -> return next == SeasonalFreightInvoiceManifestStatus.ACTIVE || next == SeasonalFreightInvoiceManifestStatus.SUSPENDED
            SeasonalFreightInvoiceManifestStatus.ACTIVE -> {
                when (next) {
                    SeasonalFreightInvoiceManifestStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalFreightInvoiceManifestStatus.SUSPENDED -> {
                when (next) {
                    SeasonalFreightInvoiceManifestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalFreightInvoiceManifestStatus) {
        if (!canMoveTo(next)) {
            error("seasonal freight invoice manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal freight invoice manifest"} $code"

    fun effectiveLoad(): Double = round(limit.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): SeasonalFreightInvoiceManifest {
        return SeasonalFreightInvoiceManifest(
            code = code,
            label = label,
            margin = margin,
            limit = limit,
            floorValue = floorValue,
            category = category,
            threshold = threshold,
            attempts = attempts,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal freight invoice manifest in status draft. */
fun newSeasonalFreightInvoiceManifest(code: String, label: String): SeasonalFreightInvoiceManifest {
    return SeasonalFreightInvoiceManifest(
        code = code,
        label = label,
        margin = 0.0,
        limit = 0,
        floorValue = 0.0,
        category = "",
        threshold = 0.0,
        attempts = 0,
        country = "",
        status = SeasonalFreightInvoiceManifestStatus.DRAFT,
    )
}
