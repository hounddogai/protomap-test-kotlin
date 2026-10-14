package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending dock door tier is in its life cycle. */
enum class PendingDockDoorTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending dock door tier, with what the planners need to act on it. */
data class PendingDockDoorTier(
    var code: String,
    var label: String,
    var timezone: String,
    var stopCount: Int,
    var region: String,
    var odometer: Int,
    var ceiling: Double,
    var graceDays: Int,
    var updatedAt: Int,
    var status: PendingDockDoorTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDockDoorTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDockDoorTierStatus): Boolean {
        when (status) {
            PendingDockDoorTierStatus.CREATED -> return next == PendingDockDoorTierStatus.RETIRED
            PendingDockDoorTierStatus.VALIDATED -> {
                when (next) {
                    PendingDockDoorTierStatus.RETIRED -> return true
                }
                return false
            }
            PendingDockDoorTierStatus.PUBLISHED -> {
                when (next) {
                    PendingDockDoorTierStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDockDoorTierStatus) {
        if (!canMoveTo(next)) {
            error("pending dock door tier $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"pending dock door tier"} $code"

    fun projectedExposure(): Double = round((stopCount.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): PendingDockDoorTier {
        return PendingDockDoorTier(
            code = code,
            label = label,
            timezone = timezone,
            stopCount = stopCount,
            region = region,
            odometer = odometer,
            ceiling = ceiling,
            graceDays = graceDays,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending dock door tier in status created. */
fun newPendingDockDoorTier(code: String, label: String): PendingDockDoorTier {
    return PendingDockDoorTier(
        code = code,
        label = label,
        timezone = "",
        stopCount = 0,
        region = "",
        odometer = 0,
        ceiling = 0.0,
        graceDays = 0,
        updatedAt = 0,
        status = PendingDockDoorTierStatus.CREATED,
    )
}
