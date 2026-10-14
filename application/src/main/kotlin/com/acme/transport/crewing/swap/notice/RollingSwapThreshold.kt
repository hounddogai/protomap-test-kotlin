package com.acme.transport.crewing.swap.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling swap threshold is in its life cycle. */
enum class RollingSwapThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rolling swap threshold and its current status. */
data class RollingSwapThreshold(
    var code: String,
    var title: String,
    var locale: String,
    var version: Int,
    var driverId: String,
    var startsAt: Int,
    var limit: Int,
    var reviewer: String,
    var cost: Double,
    var externalRef: String,
    var threshold: Double,
    var status: RollingSwapThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSwapThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSwapThresholdStatus): Boolean {
        when (status) {
            RollingSwapThresholdStatus.PLANNED -> return next == RollingSwapThresholdStatus.COMPLETED
            RollingSwapThresholdStatus.SCHEDULED -> return next == RollingSwapThresholdStatus.CANCELLED
            RollingSwapThresholdStatus.IN_PROGRESS -> return next == RollingSwapThresholdStatus.COMPLETED || next == RollingSwapThresholdStatus.CANCELLED
            RollingSwapThresholdStatus.COMPLETED -> return next == RollingSwapThresholdStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingSwapThresholdStatus) {
        if (!canMoveTo(next)) {
            error("rolling swap threshold $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun netLoad(): Double = round((threshold - startsAt.toDouble()) * 100) / 100

    fun snapshot(): RollingSwapThreshold {
        return RollingSwapThreshold(
            code = code,
            title = title,
            locale = locale,
            version = version,
            driverId = driverId,
            startsAt = startsAt,
            limit = limit,
            reviewer = reviewer,
            cost = cost,
            externalRef = externalRef,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling swap threshold in status planned. */
fun newRollingSwapThreshold(code: String, title: String): RollingSwapThreshold {
    return RollingSwapThreshold(
        code = code,
        title = title,
        locale = "",
        version = 0,
        driverId = "",
        startsAt = 0,
        limit = 0,
        reviewer = "",
        cost = 0.0,
        externalRef = "",
        threshold = 0.0,
        status = RollingSwapThresholdStatus.PLANNED,
    )
}
