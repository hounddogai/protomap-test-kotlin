package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive freight invoice manifest is in its life cycle. */
enum class AdaptiveFreightInvoiceManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive freight invoice manifest. Its code is unique within a tenant. */
data class AdaptiveFreightInvoiceManifest(
    var code: String,
    var name: String,
    var priority: Int,
    var capacity: Int,
    var shiftCode: String,
    var segment: String,
    var summary: String,
    var ceiling: Double,
    var title: String,
    var status: AdaptiveFreightInvoiceManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveFreightInvoiceManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveFreightInvoiceManifestStatus): Boolean {
        when (status) {
            AdaptiveFreightInvoiceManifestStatus.NEW -> {
                when (next) {
                    AdaptiveFreightInvoiceManifestStatus.RUNNING -> return true
                }
                return false
            }
            AdaptiveFreightInvoiceManifestStatus.QUEUED -> {
                when (next) {
                    AdaptiveFreightInvoiceManifestStatus.DONE -> return true
                }
                return false
            }
            AdaptiveFreightInvoiceManifestStatus.RUNNING -> {
                when (next) {
                    AdaptiveFreightInvoiceManifestStatus.DONE, AdaptiveFreightInvoiceManifestStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveFreightInvoiceManifestStatus.DONE -> return next == AdaptiveFreightInvoiceManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveFreightInvoiceManifestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive freight invoice manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedLoad(): Double = round((ceiling - priority.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveFreightInvoiceManifest {
        return AdaptiveFreightInvoiceManifest(
            code = code,
            name = name,
            priority = priority,
            capacity = capacity,
            shiftCode = shiftCode,
            segment = segment,
            summary = summary,
            ceiling = ceiling,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a adaptive freight invoice manifest in status new. */
fun newAdaptiveFreightInvoiceManifest(code: String, name: String): AdaptiveFreightInvoiceManifest {
    return AdaptiveFreightInvoiceManifest(
        code = code,
        name = name,
        priority = 0,
        capacity = 0,
        shiftCode = "",
        segment = "",
        summary = "",
        ceiling = 0.0,
        title = "",
        status = AdaptiveFreightInvoiceManifestStatus.NEW,
    )
}
