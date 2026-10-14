package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal route offset notice records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a seasonal route offset notice is in its life cycle. */
enum class SeasonalRouteOffsetNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a seasonal route offset notice and its current status. */
data class SeasonalRouteOffsetNotice(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var endsAt: Int,
    var expiresAt: Int,
    var priority: Int,
    var reviewer: String,
    var threshold: Double,
    var loadKg: Double,
    var title: String,
    var quantity: Int,
    var status: SeasonalRouteOffsetNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRouteOffsetNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRouteOffsetNoticeStatus): Boolean {
        when (status) {
            SeasonalRouteOffsetNoticeStatus.OPEN -> {
                when (next) {
                    SeasonalRouteOffsetNoticeStatus.IN_REVIEW, SeasonalRouteOffsetNoticeStatus.RESOLVED -> return true
                }
                return false
            }
            SeasonalRouteOffsetNoticeStatus.IN_REVIEW -> return next == SeasonalRouteOffsetNoticeStatus.REOPENED || next == SeasonalRouteOffsetNoticeStatus.ARCHIVED
            SeasonalRouteOffsetNoticeStatus.RESOLVED -> return next == SeasonalRouteOffsetNoticeStatus.ARCHIVED
            SeasonalRouteOffsetNoticeStatus.REOPENED -> return next == SeasonalRouteOffsetNoticeStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalRouteOffsetNoticeStatus) {
        if (!canMoveTo(next)) {
            error("seasonal route offset notice $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal route offset notice"} $code"

    fun effectiveVolume(): Double = round((quantity.toDouble() - threshold) * 100) / 100

    fun snapshot(): SeasonalRouteOffsetNotice {
        return SeasonalRouteOffsetNotice(
            code = code,
            name = name,
            updatedAt = updatedAt,
            endsAt = endsAt,
            expiresAt = expiresAt,
            priority = priority,
            reviewer = reviewer,
            threshold = threshold,
            loadKg = loadKg,
            title = title,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal route offset notice in status open. */
fun newSeasonalRouteOffsetNotice(code: String, name: String): SeasonalRouteOffsetNotice {
    return SeasonalRouteOffsetNotice(
        code = code,
        name = name,
        updatedAt = 0,
        endsAt = 0,
        expiresAt = 0,
        priority = 0,
        reviewer = "",
        threshold = 0.0,
        loadKg = 0.0,
        title = "",
        quantity = 0,
        status = SeasonalRouteOffsetNoticeStatus.OPEN,
    )
}
