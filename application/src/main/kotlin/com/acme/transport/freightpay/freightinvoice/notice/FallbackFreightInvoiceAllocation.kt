package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback freight invoice allocation is in its life cycle. */
enum class FallbackFreightInvoiceAllocationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback freight invoice allocation as the freight invoice team keeps it. */
data class FallbackFreightInvoiceAllocation(
    var code: String,
    var name: String,
    var windowDays: Int,
    var description: String,
    var archived: Boolean,
    var contactEmail: String,
    var distanceKm: Double,
    var unit: String,
    var odometer: Int,
    var quantity: Int,
    var tags: MutableList<String>,
    var status: FallbackFreightInvoiceAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackFreightInvoiceAllocationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackFreightInvoiceAllocationStatus): Boolean {
        when (status) {
            FallbackFreightInvoiceAllocationStatus.OPEN -> {
                when (next) {
                    FallbackFreightInvoiceAllocationStatus.IN_REVIEW, FallbackFreightInvoiceAllocationStatus.RESOLVED -> return true
                }
                return false
            }
            FallbackFreightInvoiceAllocationStatus.IN_REVIEW -> {
                when (next) {
                    FallbackFreightInvoiceAllocationStatus.RESOLVED, FallbackFreightInvoiceAllocationStatus.REOPENED -> return true
                }
                return false
            }
            FallbackFreightInvoiceAllocationStatus.RESOLVED -> {
                when (next) {
                    FallbackFreightInvoiceAllocationStatus.REOPENED, FallbackFreightInvoiceAllocationStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackFreightInvoiceAllocationStatus.REOPENED -> return next == FallbackFreightInvoiceAllocationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackFreightInvoiceAllocationStatus) {
        if (!canMoveTo(next)) {
            error("fallback freight invoice allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback freight invoice allocation"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((distanceKm + quantity.toDouble()) * 100) / 100

    fun snapshot(): FallbackFreightInvoiceAllocation {
        return FallbackFreightInvoiceAllocation(
            code = code,
            name = name,
            windowDays = windowDays,
            description = description,
            archived = archived,
            contactEmail = contactEmail,
            distanceKm = distanceKm,
            unit = unit,
            odometer = odometer,
            quantity = quantity,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback freight invoice allocation in status open. */
fun newFallbackFreightInvoiceAllocation(code: String, name: String): FallbackFreightInvoiceAllocation {
    return FallbackFreightInvoiceAllocation(
        code = code,
        name = name,
        windowDays = 0,
        description = "",
        archived = false,
        contactEmail = "",
        distanceKm = 0.0,
        unit = "",
        odometer = 0,
        quantity = 0,
        tags = mutableListOf<String>(),
        status = FallbackFreightInvoiceAllocationStatus.OPEN,
    )
}
