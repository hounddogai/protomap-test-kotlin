package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional fuel surcharge audit records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a regional fuel surcharge audit is in its life cycle. */
enum class RegionalFuelSurchargeAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional fuel surcharge audit. Its code is unique within a tenant. */
data class RegionalFuelSurchargeAudit(
    var code: String,
    var name: String,
    var currency: String,
    var depot: String,
    var attempts: Int,
    var discount: Double,
    var durationMinutes: Int,
    var windowDays: Int,
    var pallets: Int,
    var active: Boolean,
    var odometer: Int,
    var status: RegionalFuelSurchargeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelSurchargeAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelSurchargeAuditStatus): Boolean {
        when (status) {
            RegionalFuelSurchargeAuditStatus.DRAFT -> {
                when (next) {
                    RegionalFuelSurchargeAuditStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalFuelSurchargeAuditStatus.PENDING -> return next == RegionalFuelSurchargeAuditStatus.SUSPENDED
            RegionalFuelSurchargeAuditStatus.ACTIVE -> return next == RegionalFuelSurchargeAuditStatus.SUSPENDED
            RegionalFuelSurchargeAuditStatus.SUSPENDED -> return next == RegionalFuelSurchargeAuditStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalFuelSurchargeAuditStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel surcharge audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round((discount - attempts.toDouble()) * 100) / 100

    fun snapshot(): RegionalFuelSurchargeAudit {
        return RegionalFuelSurchargeAudit(
            code = code,
            name = name,
            currency = currency,
            depot = depot,
            attempts = attempts,
            discount = discount,
            durationMinutes = durationMinutes,
            windowDays = windowDays,
            pallets = pallets,
            active = active,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a regional fuel surcharge audit in status draft. */
fun newRegionalFuelSurchargeAudit(code: String, name: String): RegionalFuelSurchargeAudit {
    return RegionalFuelSurchargeAudit(
        code = code,
        name = name,
        currency = "",
        depot = "",
        attempts = 0,
        discount = 0.0,
        durationMinutes = 0,
        windowDays = 0,
        pallets = 0,
        active = false,
        odometer = 0,
        status = RegionalFuelSurchargeAuditStatus.DRAFT,
    )
}
