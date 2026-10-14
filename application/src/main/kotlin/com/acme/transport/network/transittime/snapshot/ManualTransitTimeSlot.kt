package com.acme.transport.network.transittime.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual transit time slot is in its life cycle. */
enum class ManualTransitTimeSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual transit time slot as the transit time team keeps it. */
data class ManualTransitTimeSlot(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var loadKg: Double,
    var limit: Int,
    var amount: Double,
    var maximum: Double,
    var version: Int,
    var quantity: Int,
    var status: ManualTransitTimeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTransitTimeSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTransitTimeSlotStatus): Boolean {
        when (status) {
            ManualTransitTimeSlotStatus.DRAFT -> {
                when (next) {
                    ManualTransitTimeSlotStatus.ACTIVE -> return true
                }
                return false
            }
            ManualTransitTimeSlotStatus.PENDING -> {
                when (next) {
                    ManualTransitTimeSlotStatus.CLOSED -> return true
                }
                return false
            }
            ManualTransitTimeSlotStatus.ACTIVE -> {
                when (next) {
                    ManualTransitTimeSlotStatus.CLOSED -> return true
                }
                return false
            }
            ManualTransitTimeSlotStatus.SUSPENDED -> return next == ManualTransitTimeSlotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualTransitTimeSlotStatus) {
        if (!canMoveTo(next)) {
            error("manual transit time slot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun projectedExposure(): Double = round((version.toDouble() + loadKg) * 100) / 100

    fun snapshot(): ManualTransitTimeSlot {
        return ManualTransitTimeSlot(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            loadKg = loadKg,
            limit = limit,
            amount = amount,
            maximum = maximum,
            version = version,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual transit time slot in status draft. */
fun newManualTransitTimeSlot(code: String, name: String): ManualTransitTimeSlot {
    return ManualTransitTimeSlot(
        code = code,
        name = name,
        durationMinutes = 0,
        loadKg = 0.0,
        limit = 0,
        amount = 0.0,
        maximum = 0.0,
        version = 0,
        quantity = 0,
        status = ManualTransitTimeSlotStatus.DRAFT,
    )
}
