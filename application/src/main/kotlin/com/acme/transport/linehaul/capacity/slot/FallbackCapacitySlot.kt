package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback capacity slot records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a fallback capacity slot is in its life cycle. */
enum class FallbackCapacitySlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback capacity slot, with what the planners need to act on it. */
data class FallbackCapacitySlot(
    var code: String,
    var label: String,
    var category: String,
    var reviewer: String,
    var checksum: String,
    var weight: Double,
    var pallets: Int,
    var timezone: String,
    var title: String,
    var status: FallbackCapacitySlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCapacitySlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCapacitySlotStatus): Boolean {
        when (status) {
            FallbackCapacitySlotStatus.DRAFT -> {
                when (next) {
                    FallbackCapacitySlotStatus.PENDING, FallbackCapacitySlotStatus.CLOSED -> return true
                }
                return false
            }
            FallbackCapacitySlotStatus.PENDING -> return next == FallbackCapacitySlotStatus.SUSPENDED || next == FallbackCapacitySlotStatus.CLOSED
            FallbackCapacitySlotStatus.ACTIVE -> return next == FallbackCapacitySlotStatus.SUSPENDED || next == FallbackCapacitySlotStatus.CLOSED
            FallbackCapacitySlotStatus.SUSPENDED -> return next == FallbackCapacitySlotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackCapacitySlotStatus) {
        if (!canMoveTo(next)) {
            error("fallback capacity slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback capacity slot"} $code"

    fun netTotal(): Double = round(weight * pallets.toDouble() * 100) / 100

    fun snapshot(): FallbackCapacitySlot {
        return FallbackCapacitySlot(
            code = code,
            label = label,
            category = category,
            reviewer = reviewer,
            checksum = checksum,
            weight = weight,
            pallets = pallets,
            timezone = timezone,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback capacity slot in status draft. */
fun newFallbackCapacitySlot(code: String, label: String): FallbackCapacitySlot {
    return FallbackCapacitySlot(
        code = code,
        label = label,
        category = "",
        reviewer = "",
        checksum = "",
        weight = 0.0,
        pallets = 0,
        timezone = "",
        title = "",
        status = FallbackCapacitySlotStatus.DRAFT,
    )
}
