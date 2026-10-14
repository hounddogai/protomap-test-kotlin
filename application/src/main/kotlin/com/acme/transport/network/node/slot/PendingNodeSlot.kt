package com.acme.transport.network.node.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending node slot records of network's node work, and the rules, stores, and calculations around them.

/** Where a pending node slot is in its life cycle. */
enum class PendingNodeSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending node slot. Its code is unique within a tenant. */
data class PendingNodeSlot(
    var code: String,
    var title: String,
    var stopCount: Int,
    var etaMinutes: Int,
    var version: Int,
    var odometer: Int,
    var distanceKm: Double,
    var amount: Double,
    var status: PendingNodeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNodeSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNodeSlotStatus): Boolean {
        when (status) {
            PendingNodeSlotStatus.CREATED -> return next == PendingNodeSlotStatus.RETIRED
            PendingNodeSlotStatus.VALIDATED -> return next == PendingNodeSlotStatus.PUBLISHED || next == PendingNodeSlotStatus.RETIRED
            PendingNodeSlotStatus.PUBLISHED -> {
                when (next) {
                    PendingNodeSlotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingNodeSlotStatus) {
        if (!canMoveTo(next)) {
            error("pending node slot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun adjustedExposure(): Double = round((distanceKm + amount) * 100) / 100

    fun snapshot(): PendingNodeSlot {
        return PendingNodeSlot(
            code = code,
            title = title,
            stopCount = stopCount,
            etaMinutes = etaMinutes,
            version = version,
            odometer = odometer,
            distanceKm = distanceKm,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending node slot in status created. */
fun newPendingNodeSlot(code: String, title: String): PendingNodeSlot {
    return PendingNodeSlot(
        code = code,
        title = title,
        stopCount = 0,
        etaMinutes = 0,
        version = 0,
        odometer = 0,
        distanceKm = 0.0,
        amount = 0.0,
        status = PendingNodeSlotStatus.CREATED,
    )
}
