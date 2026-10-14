package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback backhaul cycle is in its life cycle. */
enum class FallbackBackhaulCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fallback backhaul cycle and its current status. */
data class FallbackBackhaulCycle(
    var code: String,
    var title: String,
    var startsAt: Int,
    var floorValue: Double,
    var loadKg: Double,
    var capacity: Int,
    var expiresAt: Int,
    var summary: String,
    var checksum: String,
    var driverId: String,
    var status: FallbackBackhaulCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackBackhaulCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackBackhaulCycleStatus): Boolean {
        when (status) {
            FallbackBackhaulCycleStatus.IDLE -> return next == FallbackBackhaulCycleStatus.STOPPED
            FallbackBackhaulCycleStatus.WARMING -> {
                when (next) {
                    FallbackBackhaulCycleStatus.READY, FallbackBackhaulCycleStatus.STOPPED -> return true
                }
                return false
            }
            FallbackBackhaulCycleStatus.READY -> return next == FallbackBackhaulCycleStatus.STOPPED
            FallbackBackhaulCycleStatus.DRAINING -> return next == FallbackBackhaulCycleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackBackhaulCycleStatus) {
        if (!canMoveTo(next)) {
            error("fallback backhaul cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedValue(): Double = round((startsAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): FallbackBackhaulCycle {
        return FallbackBackhaulCycle(
            code = code,
            title = title,
            startsAt = startsAt,
            floorValue = floorValue,
            loadKg = loadKg,
            capacity = capacity,
            expiresAt = expiresAt,
            summary = summary,
            checksum = checksum,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback backhaul cycle in status idle. */
fun newFallbackBackhaulCycle(code: String, title: String): FallbackBackhaulCycle {
    return FallbackBackhaulCycle(
        code = code,
        title = title,
        startsAt = 0,
        floorValue = 0.0,
        loadKg = 0.0,
        capacity = 0,
        expiresAt = 0,
        summary = "",
        checksum = "",
        driverId = "",
        status = FallbackBackhaulCycleStatus.IDLE,
    )
}
