package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default freight invoice batch records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a default freight invoice batch is in its life cycle. */
enum class DefaultFreightInvoiceBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One default freight invoice batch, with what the planners need to act on it. */
data class DefaultFreightInvoiceBatch(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var ceiling: Double,
    var routeCode: String,
    var checksum: String,
    var attributes: MutableMap<String, String>,
    var country: String,
    var ownerId: String,
    var source: String,
    var approved: Boolean,
    var status: DefaultFreightInvoiceBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultFreightInvoiceBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultFreightInvoiceBatchStatus): Boolean {
        when (status) {
            DefaultFreightInvoiceBatchStatus.CREATED -> return next == DefaultFreightInvoiceBatchStatus.VALIDATED
            DefaultFreightInvoiceBatchStatus.VALIDATED -> {
                when (next) {
                    DefaultFreightInvoiceBatchStatus.PUBLISHED, DefaultFreightInvoiceBatchStatus.RETIRED -> return true
                }
                return false
            }
            DefaultFreightInvoiceBatchStatus.PUBLISHED -> return next == DefaultFreightInvoiceBatchStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultFreightInvoiceBatchStatus) {
        if (!canMoveTo(next)) {
            error("default freight invoice batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((distanceKm + ceiling) * 100) / 100

    fun snapshot(): DefaultFreightInvoiceBatch {
        return DefaultFreightInvoiceBatch(
            code = code,
            name = name,
            distanceKm = distanceKm,
            ceiling = ceiling,
            routeCode = routeCode,
            checksum = checksum,
            attributes = attributes,
            country = country,
            ownerId = ownerId,
            source = source,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a default freight invoice batch in status created. */
fun newDefaultFreightInvoiceBatch(code: String, name: String): DefaultFreightInvoiceBatch {
    return DefaultFreightInvoiceBatch(
        code = code,
        name = name,
        distanceKm = 0.0,
        ceiling = 0.0,
        routeCode = "",
        checksum = "",
        attributes = mutableMapOf<String, String>(),
        country = "",
        ownerId = "",
        source = "",
        approved = false,
        status = DefaultFreightInvoiceBatchStatus.CREATED,
    )
}
