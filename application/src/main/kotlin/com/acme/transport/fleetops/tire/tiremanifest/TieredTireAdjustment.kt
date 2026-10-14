package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered tire adjustment is in its life cycle. */
enum class TieredTireAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered tire adjustment. Its code is unique within a tenant. */
data class TieredTireAdjustment(
    var code: String,
    var label: String,
    var startsAt: Int,
    var pinned: Boolean,
    var createdAt: Int,
    var reviewer: String,
    var status: TieredTireAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTireAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTireAdjustmentStatus): Boolean {
        when (status) {
            TieredTireAdjustmentStatus.IDLE -> {
                when (next) {
                    TieredTireAdjustmentStatus.READY, TieredTireAdjustmentStatus.DRAINING -> return true
                }
                return false
            }
            TieredTireAdjustmentStatus.WARMING -> return next == TieredTireAdjustmentStatus.DRAINING
            TieredTireAdjustmentStatus.READY -> return next == TieredTireAdjustmentStatus.DRAINING
            TieredTireAdjustmentStatus.DRAINING -> {
                when (next) {
                    TieredTireAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTireAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered tire adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round(startsAt.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): TieredTireAdjustment {
        return TieredTireAdjustment(
            code = code,
            label = label,
            startsAt = startsAt,
            pinned = pinned,
            createdAt = createdAt,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered tire adjustment in status idle. */
fun newTieredTireAdjustment(code: String, label: String): TieredTireAdjustment {
    return TieredTireAdjustment(
        code = code,
        label = label,
        startsAt = 0,
        pinned = false,
        createdAt = 0,
        reviewer = "",
        status = TieredTireAdjustmentStatus.IDLE,
    )
}
