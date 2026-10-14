package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback zone slot is in its life cycle. */
enum class FallbackZoneSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback zone slot as the zone team keeps it. */
data class FallbackZoneSlot(
    var code: String,
    var title: String,
    var checksum: String,
    var startsAt: Int,
    var country: String,
    var weight: Double,
    var maximum: Double,
    var dock: String,
    var margin: Double,
    var status: FallbackZoneSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackZoneSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackZoneSlotStatus): Boolean {
        when (status) {
            FallbackZoneSlotStatus.NEW -> {
                when (next) {
                    FallbackZoneSlotStatus.QUEUED, FallbackZoneSlotStatus.DONE -> return true
                }
                return false
            }
            FallbackZoneSlotStatus.QUEUED -> {
                when (next) {
                    FallbackZoneSlotStatus.FAILED -> return true
                }
                return false
            }
            FallbackZoneSlotStatus.RUNNING -> {
                when (next) {
                    FallbackZoneSlotStatus.DONE, FallbackZoneSlotStatus.FAILED -> return true
                }
                return false
            }
            FallbackZoneSlotStatus.DONE -> {
                when (next) {
                    FallbackZoneSlotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackZoneSlotStatus) {
        if (!canMoveTo(next)) {
            error("fallback zone slot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedLoad(): Double = round((maximum + margin) * 100) / 100

    fun snapshot(): FallbackZoneSlot {
        return FallbackZoneSlot(
            code = code,
            title = title,
            checksum = checksum,
            startsAt = startsAt,
            country = country,
            weight = weight,
            maximum = maximum,
            dock = dock,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fallback zone slot in status new. */
fun newFallbackZoneSlot(code: String, title: String): FallbackZoneSlot {
    return FallbackZoneSlot(
        code = code,
        title = title,
        checksum = "",
        startsAt = 0,
        country = "",
        weight = 0.0,
        maximum = 0.0,
        dock = "",
        margin = 0.0,
        status = FallbackZoneSlotStatus.NEW,
    )
}
