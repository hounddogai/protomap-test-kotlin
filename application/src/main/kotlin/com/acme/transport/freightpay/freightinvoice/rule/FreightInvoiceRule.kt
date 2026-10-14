package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice rule records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice rule is in its life cycle. */
enum class FreightInvoiceRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A freight invoice rule as the freight invoice team keeps it. */
data class FreightInvoiceRule(
    var code: String,
    var name: String,
    var quantity: Int,
    var odometer: Int,
    var title: String,
    var maximum: Double,
    var driverId: String,
    var durationMinutes: Int,
    var version: Int,
    var fuelLiters: Double,
    var status: FreightInvoiceRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceRuleStatus): Boolean {
        when (status) {
            FreightInvoiceRuleStatus.DRAFT -> return next == FreightInvoiceRuleStatus.PENDING
            FreightInvoiceRuleStatus.PENDING -> return next == FreightInvoiceRuleStatus.SUSPENDED
            FreightInvoiceRuleStatus.ACTIVE -> {
                when (next) {
                    FreightInvoiceRuleStatus.SUSPENDED, FreightInvoiceRuleStatus.CLOSED -> return true
                }
                return false
            }
            FreightInvoiceRuleStatus.SUSPENDED -> {
                when (next) {
                    FreightInvoiceRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceRuleStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice rule $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun adjustedLoad(): Double = round((fuelLiters + odometer.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceRule {
        return FreightInvoiceRule(
            code = code,
            name = name,
            quantity = quantity,
            odometer = odometer,
            title = title,
            maximum = maximum,
            driverId = driverId,
            durationMinutes = durationMinutes,
            version = version,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a freight invoice rule in status draft. */
fun newFreightInvoiceRule(code: String, name: String): FreightInvoiceRule {
    return FreightInvoiceRule(
        code = code,
        name = name,
        quantity = 0,
        odometer = 0,
        title = "",
        maximum = 0.0,
        driverId = "",
        durationMinutes = 0,
        version = 0,
        fuelLiters = 0.0,
        status = FreightInvoiceRuleStatus.DRAFT,
    )
}
