package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal service area threshold records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a seasonal service area threshold is in its life cycle. */
enum class SeasonalServiceAreaThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A seasonal service area threshold. Its code is unique within a tenant. */
data class SeasonalServiceAreaThreshold(
    var code: String,
    var name: String,
    var region: String,
    var priority: Int,
    var driverId: String,
    var createdAt: Int,
    var notes: String,
    var status: SeasonalServiceAreaThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalServiceAreaThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalServiceAreaThresholdStatus): Boolean {
        when (status) {
            SeasonalServiceAreaThresholdStatus.CREATED -> return next == SeasonalServiceAreaThresholdStatus.PUBLISHED || next == SeasonalServiceAreaThresholdStatus.RETIRED
            SeasonalServiceAreaThresholdStatus.VALIDATED -> return next == SeasonalServiceAreaThresholdStatus.PUBLISHED || next == SeasonalServiceAreaThresholdStatus.RETIRED
            SeasonalServiceAreaThresholdStatus.PUBLISHED -> {
                when (next) {
                    SeasonalServiceAreaThresholdStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalServiceAreaThresholdStatus) {
        if (!canMoveTo(next)) {
            error("seasonal service area threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal service area threshold"} $code"

    fun weightedExposure(): Double = round((createdAt.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): SeasonalServiceAreaThreshold {
        return SeasonalServiceAreaThreshold(
            code = code,
            name = name,
            region = region,
            priority = priority,
            driverId = driverId,
            createdAt = createdAt,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal service area threshold in status created. */
fun newSeasonalServiceAreaThreshold(code: String, name: String): SeasonalServiceAreaThreshold {
    return SeasonalServiceAreaThreshold(
        code = code,
        name = name,
        region = "",
        priority = 0,
        driverId = "",
        createdAt = 0,
        notes = "",
        status = SeasonalServiceAreaThresholdStatus.CREATED,
    )
}
