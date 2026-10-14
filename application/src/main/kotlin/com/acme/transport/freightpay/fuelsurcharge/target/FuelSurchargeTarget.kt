package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel surcharge target records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge target is in its life cycle. */
enum class FuelSurchargeTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fuel surcharge target, with what the planners need to act on it. */
data class FuelSurchargeTarget(
    var code: String,
    var name: String,
    var amount: Double,
    var title: String,
    var floorValue: Double,
    var contactEmail: String,
    var description: String,
    var externalRef: String,
    var currency: String,
    var status: FuelSurchargeTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeTargetStatus): Boolean {
        when (status) {
            FuelSurchargeTargetStatus.DRAFT -> return next == FuelSurchargeTargetStatus.CLOSED
            FuelSurchargeTargetStatus.PENDING -> return next == FuelSurchargeTargetStatus.ACTIVE || next == FuelSurchargeTargetStatus.CLOSED
            FuelSurchargeTargetStatus.ACTIVE -> return next == FuelSurchargeTargetStatus.SUSPENDED || next == FuelSurchargeTargetStatus.CLOSED
            FuelSurchargeTargetStatus.SUSPENDED -> return next == FuelSurchargeTargetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeTargetStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netValue(): Double = round((amount + floorValue) * 100) / 100

    fun snapshot(): FuelSurchargeTarget {
        return FuelSurchargeTarget(
            code = code,
            name = name,
            amount = amount,
            title = title,
            floorValue = floorValue,
            contactEmail = contactEmail,
            description = description,
            externalRef = externalRef,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel surcharge target in status draft. */
fun newFuelSurchargeTarget(code: String, name: String): FuelSurchargeTarget {
    return FuelSurchargeTarget(
        code = code,
        name = name,
        amount = 0.0,
        title = "",
        floorValue = 0.0,
        contactEmail = "",
        description = "",
        externalRef = "",
        currency = "",
        status = FuelSurchargeTargetStatus.DRAFT,
    )
}
