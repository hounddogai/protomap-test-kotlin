package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional accessorial slot is in its life cycle. */
enum class RegionalAccessorialSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional accessorial slot as the accessorial team keeps it. */
data class RegionalAccessorialSlot(
    var code: String,
    var label: String,
    var lane: String,
    var notes: String,
    var quantity: Int,
    var timezone: String,
    var durationMinutes: Int,
    var maximum: Double,
    var carrier: String,
    var status: RegionalAccessorialSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAccessorialSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAccessorialSlotStatus): Boolean {
        when (status) {
            RegionalAccessorialSlotStatus.PLANNED -> {
                when (next) {
                    RegionalAccessorialSlotStatus.COMPLETED, RegionalAccessorialSlotStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalAccessorialSlotStatus.SCHEDULED -> {
                when (next) {
                    RegionalAccessorialSlotStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RegionalAccessorialSlotStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalAccessorialSlotStatus.COMPLETED, RegionalAccessorialSlotStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalAccessorialSlotStatus.COMPLETED -> return next == RegionalAccessorialSlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RegionalAccessorialSlotStatus) {
        if (!canMoveTo(next)) {
            error("regional accessorial slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netValue(): Double = round((quantity.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): RegionalAccessorialSlot {
        return RegionalAccessorialSlot(
            code = code,
            label = label,
            lane = lane,
            notes = notes,
            quantity = quantity,
            timezone = timezone,
            durationMinutes = durationMinutes,
            maximum = maximum,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional accessorial slot in status planned. */
fun newRegionalAccessorialSlot(code: String, label: String): RegionalAccessorialSlot {
    return RegionalAccessorialSlot(
        code = code,
        label = label,
        lane = "",
        notes = "",
        quantity = 0,
        timezone = "",
        durationMinutes = 0,
        maximum = 0.0,
        carrier = "",
        status = RegionalAccessorialSlotStatus.PLANNED,
    )
}
