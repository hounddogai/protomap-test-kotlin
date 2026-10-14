package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling accessorial estimate is in its life cycle. */
enum class RollingAccessorialEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling accessorial estimate as the accessorial team keeps it. */
data class RollingAccessorialEstimate(
    var code: String,
    var title: String,
    var ownerId: String,
    var description: String,
    var margin: Double,
    var startsAt: Int,
    var quantity: Int,
    var tags: MutableList<String>,
    var unit: String,
    var etaMinutes: Int,
    var status: RollingAccessorialEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAccessorialEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAccessorialEstimateStatus): Boolean {
        when (status) {
            RollingAccessorialEstimateStatus.IDLE -> return next == RollingAccessorialEstimateStatus.DRAINING
            RollingAccessorialEstimateStatus.WARMING -> {
                when (next) {
                    RollingAccessorialEstimateStatus.READY, RollingAccessorialEstimateStatus.DRAINING -> return true
                }
                return false
            }
            RollingAccessorialEstimateStatus.READY -> return next == RollingAccessorialEstimateStatus.STOPPED
            RollingAccessorialEstimateStatus.DRAINING -> return next == RollingAccessorialEstimateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingAccessorialEstimateStatus) {
        if (!canMoveTo(next)) {
            error("rolling accessorial estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round((etaMinutes.toDouble() - margin) * 100) / 100

    fun snapshot(): RollingAccessorialEstimate {
        return RollingAccessorialEstimate(
            code = code,
            title = title,
            ownerId = ownerId,
            description = description,
            margin = margin,
            startsAt = startsAt,
            quantity = quantity,
            tags = tags,
            unit = unit,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling accessorial estimate in status idle. */
fun newRollingAccessorialEstimate(code: String, title: String): RollingAccessorialEstimate {
    return RollingAccessorialEstimate(
        code = code,
        title = title,
        ownerId = "",
        description = "",
        margin = 0.0,
        startsAt = 0,
        quantity = 0,
        tags = mutableListOf<String>(),
        unit = "",
        etaMinutes = 0,
        status = RollingAccessorialEstimateStatus.IDLE,
    )
}
