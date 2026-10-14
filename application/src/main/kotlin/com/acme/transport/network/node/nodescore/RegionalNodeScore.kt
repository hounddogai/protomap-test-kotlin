package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional node score records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a regional node score is in its life cycle. */
enum class RegionalNodeScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One regional node score, with what the planners need to act on it. */
data class RegionalNodeScore(
    var code: String,
    var name: String,
    var batchSize: Int,
    var active: Boolean,
    var timezone: String,
    var notes: String,
    var approved: Boolean,
    var floorValue: Double,
    var depot: String,
    var windowDays: Int,
    var status: RegionalNodeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalNodeScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalNodeScoreStatus): Boolean {
        when (status) {
            RegionalNodeScoreStatus.CREATED -> return next == RegionalNodeScoreStatus.PUBLISHED
            RegionalNodeScoreStatus.VALIDATED -> {
                when (next) {
                    RegionalNodeScoreStatus.RETIRED -> return true
                }
                return false
            }
            RegionalNodeScoreStatus.PUBLISHED -> return next == RegionalNodeScoreStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalNodeScoreStatus) {
        if (!canMoveTo(next)) {
            error("regional node score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional node score"} $code"

    fun netExposure(): Double = round((windowDays.toDouble() + floorValue) * 100) / 100

    fun snapshot(): RegionalNodeScore {
        return RegionalNodeScore(
            code = code,
            name = name,
            batchSize = batchSize,
            active = active,
            timezone = timezone,
            notes = notes,
            approved = approved,
            floorValue = floorValue,
            depot = depot,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a regional node score in status created. */
fun newRegionalNodeScore(code: String, name: String): RegionalNodeScore {
    return RegionalNodeScore(
        code = code,
        name = name,
        batchSize = 0,
        active = false,
        timezone = "",
        notes = "",
        approved = false,
        floorValue = 0.0,
        depot = "",
        windowDays = 0,
        status = RegionalNodeScoreStatus.CREATED,
    )
}
