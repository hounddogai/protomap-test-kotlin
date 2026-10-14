package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped freight invoice plan is in its life cycle. */
enum class CappedFreightInvoicePlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped freight invoice plan as the freight invoice team keeps it. */
data class CappedFreightInvoicePlan(
    var code: String,
    var label: String,
    var trailerId: String,
    var segment: String,
    var windowDays: Int,
    var version: Int,
    var vehicleId: String,
    var status: CappedFreightInvoicePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedFreightInvoicePlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedFreightInvoicePlanStatus): Boolean {
        when (status) {
            CappedFreightInvoicePlanStatus.DRAFT -> return next == CappedFreightInvoicePlanStatus.CLOSED
            CappedFreightInvoicePlanStatus.PENDING -> {
                when (next) {
                    CappedFreightInvoicePlanStatus.SUSPENDED, CappedFreightInvoicePlanStatus.CLOSED -> return true
                }
                return false
            }
            CappedFreightInvoicePlanStatus.ACTIVE -> return next == CappedFreightInvoicePlanStatus.CLOSED
            CappedFreightInvoicePlanStatus.SUSPENDED -> return next == CappedFreightInvoicePlanStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedFreightInvoicePlanStatus) {
        if (!canMoveTo(next)) {
            error("capped freight invoice plan $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun netLoad(): Double = round(version.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): CappedFreightInvoicePlan {
        return CappedFreightInvoicePlan(
            code = code,
            label = label,
            trailerId = trailerId,
            segment = segment,
            windowDays = windowDays,
            version = version,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped freight invoice plan in status draft. */
fun newCappedFreightInvoicePlan(code: String, label: String): CappedFreightInvoicePlan {
    return CappedFreightInvoicePlan(
        code = code,
        label = label,
        trailerId = "",
        segment = "",
        windowDays = 0,
        version = 0,
        vehicleId = "",
        status = CappedFreightInvoicePlanStatus.DRAFT,
    )
}
