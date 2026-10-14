package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer quota is in its life cycle. */
enum class TrailerQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One trailer quota, with what the planners need to act on it. */
data class TrailerQuota(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var limit: Int,
    var approved: Boolean,
    var weight: Double,
    var priority: Int,
    var pallets: Int,
    var startsAt: Int,
    var status: TrailerQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerQuotaStatus): Boolean {
        when (status) {
            TrailerQuotaStatus.OPEN -> {
                when (next) {
                    TrailerQuotaStatus.IN_REVIEW, TrailerQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            TrailerQuotaStatus.IN_REVIEW -> return next == TrailerQuotaStatus.REOPENED
            TrailerQuotaStatus.RESOLVED -> {
                when (next) {
                    TrailerQuotaStatus.REOPENED, TrailerQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            TrailerQuotaStatus.REOPENED -> return next == TrailerQuotaStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TrailerQuotaStatus) {
        if (!canMoveTo(next)) {
            error("trailer quota $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"trailer quota"} $code"

    fun netExposure(): Double = round((priority.toDouble() + weight) * 100) / 100

    fun snapshot(): TrailerQuota {
        return TrailerQuota(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            limit = limit,
            approved = approved,
            weight = weight,
            priority = priority,
            pallets = pallets,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer quota in status open. */
fun newTrailerQuota(code: String, title: String): TrailerQuota {
    return TrailerQuota(
        code = code,
        title = title,
        durationMinutes = 0,
        limit = 0,
        approved = false,
        weight = 0.0,
        priority = 0,
        pallets = 0,
        startsAt = 0,
        status = TrailerQuotaStatus.OPEN,
    )
}
