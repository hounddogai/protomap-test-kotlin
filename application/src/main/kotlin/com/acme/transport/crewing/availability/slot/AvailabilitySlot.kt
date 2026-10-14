package com.acme.transport.crewing.availability.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability slot records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability slot is in its life cycle. */
enum class AvailabilitySlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a availability slot and its current status. */
data class AvailabilitySlot(
    var code: String,
    var label: String,
    var retries: Int,
    var etaMinutes: Int,
    var carrier: String,
    var attempts: Int,
    var budget: Double,
    var createdAt: Int,
    var dock: String,
    var graceDays: Int,
    var status: AvailabilitySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilitySlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilitySlotStatus): Boolean {
        when (status) {
            AvailabilitySlotStatus.REQUESTED -> return next == AvailabilitySlotStatus.GRANTED || next == AvailabilitySlotStatus.EXPIRED
            AvailabilitySlotStatus.GRANTED -> {
                when (next) {
                    AvailabilitySlotStatus.REVOKED, AvailabilitySlotStatus.EXPIRED -> return true
                }
                return false
            }
            AvailabilitySlotStatus.REVOKED -> return next == AvailabilitySlotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AvailabilitySlotStatus) {
        if (!canMoveTo(next)) {
            error("availability slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedExposure(): Double = round((retries.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): AvailabilitySlot {
        return AvailabilitySlot(
            code = code,
            label = label,
            retries = retries,
            etaMinutes = etaMinutes,
            carrier = carrier,
            attempts = attempts,
            budget = budget,
            createdAt = createdAt,
            dock = dock,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a availability slot in status requested. */
fun newAvailabilitySlot(code: String, label: String): AvailabilitySlot {
    return AvailabilitySlot(
        code = code,
        label = label,
        retries = 0,
        etaMinutes = 0,
        carrier = "",
        attempts = 0,
        budget = 0.0,
        createdAt = 0,
        dock = "",
        graceDays = 0,
        status = AvailabilitySlotStatus.REQUESTED,
    )
}
