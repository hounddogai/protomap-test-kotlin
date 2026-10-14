package com.acme.transport.freightpay.freightinvoice.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered freight invoice target records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a tiered freight invoice target is in its life cycle. */
enum class TieredFreightInvoiceTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One tiered freight invoice target, with what the planners need to act on it. */
data class TieredFreightInvoiceTarget(
    var code: String,
    var name: String,
    var trailerId: String,
    var cost: Double,
    var pallets: Int,
    var approved: Boolean,
    var visible: Boolean,
    var description: String,
    var phone: String,
    var quantity: Int,
    var ownerId: String,
    var status: TieredFreightInvoiceTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFreightInvoiceTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFreightInvoiceTargetStatus): Boolean {
        when (status) {
            TieredFreightInvoiceTargetStatus.REQUESTED -> {
                when (next) {
                    TieredFreightInvoiceTargetStatus.GRANTED, TieredFreightInvoiceTargetStatus.REVOKED -> return true
                }
                return false
            }
            TieredFreightInvoiceTargetStatus.GRANTED -> {
                when (next) {
                    TieredFreightInvoiceTargetStatus.REVOKED -> return true
                }
                return false
            }
            TieredFreightInvoiceTargetStatus.REVOKED -> return next == TieredFreightInvoiceTargetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredFreightInvoiceTargetStatus) {
        if (!canMoveTo(next)) {
            error("tiered freight invoice target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedValue(): Double = round((pallets.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): TieredFreightInvoiceTarget {
        return TieredFreightInvoiceTarget(
            code = code,
            name = name,
            trailerId = trailerId,
            cost = cost,
            pallets = pallets,
            approved = approved,
            visible = visible,
            description = description,
            phone = phone,
            quantity = quantity,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered freight invoice target in status requested. */
fun newTieredFreightInvoiceTarget(code: String, name: String): TieredFreightInvoiceTarget {
    return TieredFreightInvoiceTarget(
        code = code,
        name = name,
        trailerId = "",
        cost = 0.0,
        pallets = 0,
        approved = false,
        visible = false,
        description = "",
        phone = "",
        quantity = 0,
        ownerId = "",
        status = TieredFreightInvoiceTargetStatus.REQUESTED,
    )
}
