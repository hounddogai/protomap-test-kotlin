package com.acme.transport.crewing.swap.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal swap rollup is in its life cycle. */
enum class SeasonalSwapRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a seasonal swap rollup and its current status. */
data class SeasonalSwapRollup(
    var code: String,
    var title: String,
    var version: Int,
    var threshold: Double,
    var dock: String,
    var attempts: Int,
    var quantity: Int,
    var visible: Boolean,
    var limit: Int,
    var status: SeasonalSwapRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalSwapRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalSwapRollupStatus): Boolean {
        when (status) {
            SeasonalSwapRollupStatus.PLANNED -> {
                when (next) {
                    SeasonalSwapRollupStatus.SCHEDULED, SeasonalSwapRollupStatus.IN_PROGRESS -> return true
                }
                return false
            }
            SeasonalSwapRollupStatus.SCHEDULED -> {
                when (next) {
                    SeasonalSwapRollupStatus.COMPLETED -> return true
                }
                return false
            }
            SeasonalSwapRollupStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalSwapRollupStatus.COMPLETED, SeasonalSwapRollupStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalSwapRollupStatus.COMPLETED -> return next == SeasonalSwapRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalSwapRollupStatus) {
        if (!canMoveTo(next)) {
            error("seasonal swap rollup $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"seasonal swap rollup"} $code"

    fun adjustedExposure(): Double = round((version.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): SeasonalSwapRollup {
        return SeasonalSwapRollup(
            code = code,
            title = title,
            version = version,
            threshold = threshold,
            dock = dock,
            attempts = attempts,
            quantity = quantity,
            visible = visible,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal swap rollup in status planned. */
fun newSeasonalSwapRollup(code: String, title: String): SeasonalSwapRollup {
    return SeasonalSwapRollup(
        code = code,
        title = title,
        version = 0,
        threshold = 0.0,
        dock = "",
        attempts = 0,
        quantity = 0,
        visible = false,
        limit = 0,
        status = SeasonalSwapRollupStatus.PLANNED,
    )
}
