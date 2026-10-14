package com.acme.transport.crewing.roster.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The roster threshold records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster threshold is in its life cycle. */
enum class RosterThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A roster threshold as the roster team keeps it. */
data class RosterThreshold(
    var code: String,
    var title: String,
    var retries: Int,
    var driverId: String,
    var attempts: Int,
    var shiftCode: String,
    var ceiling: Double,
    var createdAt: Int,
    var dock: String,
    var minimum: Double,
    var status: RosterThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterThresholdStatus): Boolean {
        when (status) {
            RosterThresholdStatus.CREATED -> {
                when (next) {
                    RosterThresholdStatus.PUBLISHED, RosterThresholdStatus.RETIRED -> return true
                }
                return false
            }
            RosterThresholdStatus.VALIDATED -> return next == RosterThresholdStatus.RETIRED
            RosterThresholdStatus.PUBLISHED -> return next == RosterThresholdStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RosterThresholdStatus) {
        if (!canMoveTo(next)) {
            error("roster threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedExposure(): Double = round((retries.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): RosterThreshold {
        return RosterThreshold(
            code = code,
            title = title,
            retries = retries,
            driverId = driverId,
            attempts = attempts,
            shiftCode = shiftCode,
            ceiling = ceiling,
            createdAt = createdAt,
            dock = dock,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a roster threshold in status created. */
fun newRosterThreshold(code: String, title: String): RosterThreshold {
    return RosterThreshold(
        code = code,
        title = title,
        retries = 0,
        driverId = "",
        attempts = 0,
        shiftCode = "",
        ceiling = 0.0,
        createdAt = 0,
        dock = "",
        minimum = 0.0,
        status = RosterThresholdStatus.CREATED,
    )
}
