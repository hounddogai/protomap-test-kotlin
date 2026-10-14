package com.acme.transport.network.zone.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback zone threshold is in its life cycle. */
enum class FallbackZoneThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One fallback zone threshold, with what the planners need to act on it. */
data class FallbackZoneThreshold(
    var code: String,
    var name: String,
    var ceiling: Double,
    var unit: String,
    var summary: String,
    var lane: String,
    var ratio: Double,
    var expiresAt: Int,
    var phone: String,
    var status: FallbackZoneThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackZoneThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackZoneThresholdStatus): Boolean {
        when (status) {
            FallbackZoneThresholdStatus.NEW -> return next == FallbackZoneThresholdStatus.DONE || next == FallbackZoneThresholdStatus.FAILED
            FallbackZoneThresholdStatus.QUEUED -> return next == FallbackZoneThresholdStatus.RUNNING || next == FallbackZoneThresholdStatus.DONE
            FallbackZoneThresholdStatus.RUNNING -> {
                when (next) {
                    FallbackZoneThresholdStatus.DONE, FallbackZoneThresholdStatus.FAILED -> return true
                }
                return false
            }
            FallbackZoneThresholdStatus.DONE -> return next == FallbackZoneThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackZoneThresholdStatus) {
        if (!canMoveTo(next)) {
            error("fallback zone threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedTotal(): Double = round(expiresAt.toDouble() * ratio * 100) / 100

    fun snapshot(): FallbackZoneThreshold {
        return FallbackZoneThreshold(
            code = code,
            name = name,
            ceiling = ceiling,
            unit = unit,
            summary = summary,
            lane = lane,
            ratio = ratio,
            expiresAt = expiresAt,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback zone threshold in status new. */
fun newFallbackZoneThreshold(code: String, name: String): FallbackZoneThreshold {
    return FallbackZoneThreshold(
        code = code,
        name = name,
        ceiling = 0.0,
        unit = "",
        summary = "",
        lane = "",
        ratio = 0.0,
        expiresAt = 0,
        phone = "",
        status = FallbackZoneThresholdStatus.NEW,
    )
}
