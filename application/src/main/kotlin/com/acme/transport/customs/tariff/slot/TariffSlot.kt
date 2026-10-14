package com.acme.transport.customs.tariff.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff slot records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff slot is in its life cycle. */
enum class TariffSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a tariff slot and its current status. */
data class TariffSlot(
    var code: String,
    var label: String,
    var priority: Int,
    var timezone: String,
    var windowDays: Int,
    var pinned: Boolean,
    var threshold: Double,
    var contactEmail: String,
    var status: TariffSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffSlotStatus): Boolean {
        when (status) {
            TariffSlotStatus.DRAFT -> return next == TariffSlotStatus.PENDING || next == TariffSlotStatus.CLOSED
            TariffSlotStatus.PENDING -> {
                when (next) {
                    TariffSlotStatus.SUSPENDED -> return true
                }
                return false
            }
            TariffSlotStatus.ACTIVE -> {
                when (next) {
                    TariffSlotStatus.SUSPENDED, TariffSlotStatus.CLOSED -> return true
                }
                return false
            }
            TariffSlotStatus.SUSPENDED -> return next == TariffSlotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TariffSlotStatus) {
        if (!canMoveTo(next)) {
            error("tariff slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round(threshold * priority.toDouble() * 100) / 100

    fun snapshot(): TariffSlot {
        return TariffSlot(
            code = code,
            label = label,
            priority = priority,
            timezone = timezone,
            windowDays = windowDays,
            pinned = pinned,
            threshold = threshold,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tariff slot in status draft. */
fun newTariffSlot(code: String, label: String): TariffSlot {
    return TariffSlot(
        code = code,
        label = label,
        priority = 0,
        timezone = "",
        windowDays = 0,
        pinned = false,
        threshold = 0.0,
        contactEmail = "",
        status = TariffSlotStatus.DRAFT,
    )
}
