package com.acme.transport.terminal.traileryard.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer yard forecast records of terminal's trailer yard work, and the rules, stores, and calculations
// around them.

/** Where a trailer yard forecast is in its life cycle. */
enum class TrailerYardForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A trailer yard forecast as the trailer yard team keeps it. */
data class TrailerYardForecast(
    var code: String,
    var label: String,
    var segment: String,
    var ceiling: Double,
    var dock: String,
    var archived: Boolean,
    var ownerId: String,
    var revision: Int,
    var status: TrailerYardForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardForecastStatus): Boolean {
        when (status) {
            TrailerYardForecastStatus.PROPOSED -> {
                when (next) {
                    TrailerYardForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            TrailerYardForecastStatus.APPROVED -> {
                when (next) {
                    TrailerYardForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            TrailerYardForecastStatus.REJECTED -> {
                when (next) {
                    TrailerYardForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardForecastStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard forecast $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"trailer yard forecast"} $code"

    fun projectedExposure(): Double = round(ceiling * revision.toDouble() * 100) / 100

    fun snapshot(): TrailerYardForecast {
        return TrailerYardForecast(
            code = code,
            label = label,
            segment = segment,
            ceiling = ceiling,
            dock = dock,
            archived = archived,
            ownerId = ownerId,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a trailer yard forecast in status proposed. */
fun newTrailerYardForecast(code: String, label: String): TrailerYardForecast {
    return TrailerYardForecast(
        code = code,
        label = label,
        segment = "",
        ceiling = 0.0,
        dock = "",
        archived = false,
        ownerId = "",
        revision = 0,
        status = TrailerYardForecastStatus.PROPOSED,
    )
}
