package com.acme.transport.freightpay.freightinvoice.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary freight invoice template records of freightpay's freight invoice work, and the rules, stores,
// and calculations around them.

/** Where a primary freight invoice template is in its life cycle. */
enum class PrimaryFreightInvoiceTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary freight invoice template as the freight invoice team keeps it. */
data class PrimaryFreightInvoiceTemplate(
    var code: String,
    var name: String,
    var ceiling: Double,
    var durationMinutes: Int,
    var approved: Boolean,
    var expiresAt: Int,
    var floorValue: Double,
    var createdAt: Int,
    var status: PrimaryFreightInvoiceTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFreightInvoiceTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFreightInvoiceTemplateStatus): Boolean {
        when (status) {
            PrimaryFreightInvoiceTemplateStatus.OPEN -> {
                when (next) {
                    PrimaryFreightInvoiceTemplateStatus.IN_REVIEW, PrimaryFreightInvoiceTemplateStatus.RESOLVED -> return true
                }
                return false
            }
            PrimaryFreightInvoiceTemplateStatus.IN_REVIEW -> return next == PrimaryFreightInvoiceTemplateStatus.ARCHIVED
            PrimaryFreightInvoiceTemplateStatus.RESOLVED -> return next == PrimaryFreightInvoiceTemplateStatus.REOPENED || next == PrimaryFreightInvoiceTemplateStatus.ARCHIVED
            PrimaryFreightInvoiceTemplateStatus.REOPENED -> {
                when (next) {
                    PrimaryFreightInvoiceTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryFreightInvoiceTemplateStatus) {
        if (!canMoveTo(next)) {
            error("primary freight invoice template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedExposure(): Double = round((durationMinutes.toDouble() - ceiling) * 100) / 100

    fun snapshot(): PrimaryFreightInvoiceTemplate {
        return PrimaryFreightInvoiceTemplate(
            code = code,
            name = name,
            ceiling = ceiling,
            durationMinutes = durationMinutes,
            approved = approved,
            expiresAt = expiresAt,
            floorValue = floorValue,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary freight invoice template in status open. */
fun newPrimaryFreightInvoiceTemplate(code: String, name: String): PrimaryFreightInvoiceTemplate {
    return PrimaryFreightInvoiceTemplate(
        code = code,
        name = name,
        ceiling = 0.0,
        durationMinutes = 0,
        approved = false,
        expiresAt = 0,
        floorValue = 0.0,
        createdAt = 0,
        status = PrimaryFreightInvoiceTemplateStatus.OPEN,
    )
}
