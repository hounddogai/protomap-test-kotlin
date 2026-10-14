package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy hub slot is in its life cycle. */
enum class LegacyHubSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a legacy hub slot and its current status. */
data class LegacyHubSlot(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var updatedAt: Int,
    var endsAt: Int,
    var country: String,
    var capacity: Int,
    var driverId: String,
    var revision: Int,
    var status: LegacyHubSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyHubSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyHubSlotStatus): Boolean {
        when (status) {
            LegacyHubSlotStatus.DRAFT -> {
                when (next) {
                    LegacyHubSlotStatus.PENDING -> return true
                }
                return false
            }
            LegacyHubSlotStatus.PENDING -> return next == LegacyHubSlotStatus.ACTIVE
            LegacyHubSlotStatus.ACTIVE -> return next == LegacyHubSlotStatus.CLOSED
            LegacyHubSlotStatus.SUSPENDED -> return next == LegacyHubSlotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LegacyHubSlotStatus) {
        if (!canMoveTo(next)) {
            error("legacy hub slot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveValue(): Double = round((revision.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyHubSlot {
        return LegacyHubSlot(
            code = code,
            label = label,
            distanceKm = distanceKm,
            updatedAt = updatedAt,
            endsAt = endsAt,
            country = country,
            capacity = capacity,
            driverId = driverId,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy hub slot in status draft. */
fun newLegacyHubSlot(code: String, label: String): LegacyHubSlot {
    return LegacyHubSlot(
        code = code,
        label = label,
        distanceKm = 0.0,
        updatedAt = 0,
        endsAt = 0,
        country = "",
        capacity = 0,
        driverId = "",
        revision = 0,
        status = LegacyHubSlotStatus.DRAFT,
    )
}
