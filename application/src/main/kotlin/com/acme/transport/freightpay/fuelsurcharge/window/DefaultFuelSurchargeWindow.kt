package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default fuel surcharge window records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a default fuel surcharge window is in its life cycle. */
enum class DefaultFuelSurchargeWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a default fuel surcharge window and its current status. */
data class DefaultFuelSurchargeWindow(
    var code: String,
    var label: String,
    var carrier: String,
    var margin: Double,
    var segment: String,
    var pallets: Int,
    var lane: String,
    var notes: String,
    var weight: Double,
    var createdAt: Int,
    var status: DefaultFuelSurchargeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultFuelSurchargeWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultFuelSurchargeWindowStatus): Boolean {
        when (status) {
            DefaultFuelSurchargeWindowStatus.DRAFT -> return next == DefaultFuelSurchargeWindowStatus.PENDING || next == DefaultFuelSurchargeWindowStatus.SUSPENDED
            DefaultFuelSurchargeWindowStatus.PENDING -> {
                when (next) {
                    DefaultFuelSurchargeWindowStatus.ACTIVE, DefaultFuelSurchargeWindowStatus.CLOSED -> return true
                }
                return false
            }
            DefaultFuelSurchargeWindowStatus.ACTIVE -> {
                when (next) {
                    DefaultFuelSurchargeWindowStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultFuelSurchargeWindowStatus.SUSPENDED -> {
                when (next) {
                    DefaultFuelSurchargeWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultFuelSurchargeWindowStatus) {
        if (!canMoveTo(next)) {
            error("default fuel surcharge window $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedLoad(): Double = round((margin - weight) * 100) / 100

    fun snapshot(): DefaultFuelSurchargeWindow {
        return DefaultFuelSurchargeWindow(
            code = code,
            label = label,
            carrier = carrier,
            margin = margin,
            segment = segment,
            pallets = pallets,
            lane = lane,
            notes = notes,
            weight = weight,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default fuel surcharge window in status draft. */
fun newDefaultFuelSurchargeWindow(code: String, label: String): DefaultFuelSurchargeWindow {
    return DefaultFuelSurchargeWindow(
        code = code,
        label = label,
        carrier = "",
        margin = 0.0,
        segment = "",
        pallets = 0,
        lane = "",
        notes = "",
        weight = 0.0,
        createdAt = 0,
        status = DefaultFuelSurchargeWindowStatus.DRAFT,
    )
}
