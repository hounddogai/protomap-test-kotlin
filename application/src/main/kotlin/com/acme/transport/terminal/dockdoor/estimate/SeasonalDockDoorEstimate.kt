package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.text.normalize

// The seasonal dock door estimate records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a seasonal dock door estimate is in its life cycle. */
enum class SeasonalDockDoorEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One seasonal dock door estimate, with what the planners need to act on it. */
data class SeasonalDockDoorEstimate(
    var code: String,
    var title: String,
    var vehicleId: String,
    var pinned: Boolean,
    var region: String,
    var phone: String,
    var score: Double,
    var status: SeasonalDockDoorEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDockDoorEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDockDoorEstimateStatus): Boolean {
        when (status) {
            SeasonalDockDoorEstimateStatus.IDLE -> return next == SeasonalDockDoorEstimateStatus.WARMING
            SeasonalDockDoorEstimateStatus.WARMING -> {
                when (next) {
                    SeasonalDockDoorEstimateStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalDockDoorEstimateStatus.READY -> return next == SeasonalDockDoorEstimateStatus.STOPPED
            SeasonalDockDoorEstimateStatus.DRAINING -> return next == SeasonalDockDoorEstimateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalDockDoorEstimateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dock door estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = score

    fun snapshot(): SeasonalDockDoorEstimate {
        return SeasonalDockDoorEstimate(
            code = code,
            title = title,
            vehicleId = vehicleId,
            pinned = pinned,
            region = region,
            phone = phone,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal dock door estimate in status idle. */
fun newSeasonalDockDoorEstimate(code: String, title: String): SeasonalDockDoorEstimate {
    return SeasonalDockDoorEstimate(
        code = code,
        title = title,
        vehicleId = "",
        pinned = false,
        region = "",
        phone = "",
        score = 0.0,
        status = SeasonalDockDoorEstimateStatus.IDLE,
    )
}
