package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal cross dock threshold is in its life cycle. */
enum class SeasonalCrossDockThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal cross dock threshold. Its code is unique within a tenant. */
data class SeasonalCrossDockThreshold(
    var code: String,
    var name: String,
    var depot: String,
    var dock: String,
    var summary: String,
    var updatedAt: Int,
    var sequence: Int,
    var trailerId: String,
    var status: SeasonalCrossDockThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCrossDockThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCrossDockThresholdStatus): Boolean {
        when (status) {
            SeasonalCrossDockThresholdStatus.DRAFT -> {
                when (next) {
                    SeasonalCrossDockThresholdStatus.PENDING, SeasonalCrossDockThresholdStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalCrossDockThresholdStatus.PENDING -> {
                when (next) {
                    SeasonalCrossDockThresholdStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalCrossDockThresholdStatus.ACTIVE -> {
                when (next) {
                    SeasonalCrossDockThresholdStatus.SUSPENDED, SeasonalCrossDockThresholdStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalCrossDockThresholdStatus.SUSPENDED -> return next == SeasonalCrossDockThresholdStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalCrossDockThresholdStatus) {
        if (!canMoveTo(next)) {
            error("seasonal cross dock threshold $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal cross dock threshold"} $code"

    fun adjustedValue(): Double = round(updatedAt.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): SeasonalCrossDockThreshold {
        return SeasonalCrossDockThreshold(
            code = code,
            name = name,
            depot = depot,
            dock = dock,
            summary = summary,
            updatedAt = updatedAt,
            sequence = sequence,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal cross dock threshold in status draft. */
fun newSeasonalCrossDockThreshold(code: String, name: String): SeasonalCrossDockThreshold {
    return SeasonalCrossDockThreshold(
        code = code,
        name = name,
        depot = "",
        dock = "",
        summary = "",
        updatedAt = 0,
        sequence = 0,
        trailerId = "",
        status = SeasonalCrossDockThresholdStatus.DRAFT,
    )
}
