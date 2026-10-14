package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending cutoff override is in its life cycle. */
enum class PendingCutoffOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending cutoff override, with what the planners need to act on it. */
data class PendingCutoffOverride(
    var code: String,
    var title: String,
    var segment: String,
    var stopCount: Int,
    var distanceKm: Double,
    var version: Int,
    var amount: Double,
    var pallets: Int,
    var trailerId: String,
    var startsAt: Int,
    var etaMinutes: Int,
    var status: PendingCutoffOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCutoffOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCutoffOverrideStatus): Boolean {
        when (status) {
            PendingCutoffOverrideStatus.PLANNED -> {
                when (next) {
                    PendingCutoffOverrideStatus.COMPLETED, PendingCutoffOverrideStatus.CANCELLED -> return true
                }
                return false
            }
            PendingCutoffOverrideStatus.SCHEDULED -> {
                when (next) {
                    PendingCutoffOverrideStatus.IN_PROGRESS, PendingCutoffOverrideStatus.COMPLETED -> return true
                }
                return false
            }
            PendingCutoffOverrideStatus.IN_PROGRESS -> return next == PendingCutoffOverrideStatus.COMPLETED
            PendingCutoffOverrideStatus.COMPLETED -> {
                when (next) {
                    PendingCutoffOverrideStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCutoffOverrideStatus) {
        if (!canMoveTo(next)) {
            error("pending cutoff override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"pending cutoff override"} $code"

    fun weightedValue(): Double = round((stopCount.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): PendingCutoffOverride {
        return PendingCutoffOverride(
            code = code,
            title = title,
            segment = segment,
            stopCount = stopCount,
            distanceKm = distanceKm,
            version = version,
            amount = amount,
            pallets = pallets,
            trailerId = trailerId,
            startsAt = startsAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a pending cutoff override in status planned. */
fun newPendingCutoffOverride(code: String, title: String): PendingCutoffOverride {
    return PendingCutoffOverride(
        code = code,
        title = title,
        segment = "",
        stopCount = 0,
        distanceKm = 0.0,
        version = 0,
        amount = 0.0,
        pallets = 0,
        trailerId = "",
        startsAt = 0,
        etaMinutes = 0,
        status = PendingCutoffOverrideStatus.PLANNED,
    )
}
