package com.acme.transport.customs.clearance.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal clearance override is in its life cycle. */
enum class SeasonalClearanceOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal clearance override. Its code is unique within a tenant. */
data class SeasonalClearanceOverride(
    var code: String,
    var title: String,
    var minimum: Double,
    var capacity: Int,
    var endsAt: Int,
    var status: SeasonalClearanceOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalClearanceOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalClearanceOverrideStatus): Boolean {
        when (status) {
            SeasonalClearanceOverrideStatus.IDLE -> {
                when (next) {
                    SeasonalClearanceOverrideStatus.DRAINING -> return true
                }
                return false
            }
            SeasonalClearanceOverrideStatus.WARMING -> {
                when (next) {
                    SeasonalClearanceOverrideStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalClearanceOverrideStatus.READY -> return next == SeasonalClearanceOverrideStatus.STOPPED
            SeasonalClearanceOverrideStatus.DRAINING -> {
                when (next) {
                    SeasonalClearanceOverrideStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalClearanceOverrideStatus) {
        if (!canMoveTo(next)) {
            error("seasonal clearance override $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round((minimum - endsAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalClearanceOverride {
        return SeasonalClearanceOverride(
            code = code,
            title = title,
            minimum = minimum,
            capacity = capacity,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal clearance override in status idle. */
fun newSeasonalClearanceOverride(code: String, title: String): SeasonalClearanceOverride {
    return SeasonalClearanceOverride(
        code = code,
        title = title,
        minimum = 0.0,
        capacity = 0,
        endsAt = 0,
        status = SeasonalClearanceOverrideStatus.IDLE,
    )
}
