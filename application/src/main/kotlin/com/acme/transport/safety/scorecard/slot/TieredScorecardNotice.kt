package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered scorecard notice is in its life cycle. */
enum class TieredScorecardNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tiered scorecard notice as the scorecard team keeps it. */
data class TieredScorecardNotice(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var trailerId: String,
    var ceiling: Double,
    var carrier: String,
    var endsAt: Int,
    var driverId: String,
    var attributes: MutableMap<String, String>,
    var startsAt: Int,
    var expiresAt: Int,
    var status: TieredScorecardNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredScorecardNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredScorecardNoticeStatus): Boolean {
        when (status) {
            TieredScorecardNoticeStatus.PLANNED -> {
                when (next) {
                    TieredScorecardNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            TieredScorecardNoticeStatus.SCHEDULED -> {
                when (next) {
                    TieredScorecardNoticeStatus.COMPLETED, TieredScorecardNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            TieredScorecardNoticeStatus.IN_PROGRESS -> return next == TieredScorecardNoticeStatus.COMPLETED || next == TieredScorecardNoticeStatus.CANCELLED
            TieredScorecardNoticeStatus.COMPLETED -> return next == TieredScorecardNoticeStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredScorecardNoticeStatus) {
        if (!canMoveTo(next)) {
            error("tiered scorecard notice $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((startsAt.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): TieredScorecardNotice {
        return TieredScorecardNotice(
            code = code,
            title = title,
            tags = tags,
            trailerId = trailerId,
            ceiling = ceiling,
            carrier = carrier,
            endsAt = endsAt,
            driverId = driverId,
            attributes = attributes,
            startsAt = startsAt,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered scorecard notice in status planned. */
fun newTieredScorecardNotice(code: String, title: String): TieredScorecardNotice {
    return TieredScorecardNotice(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        trailerId = "",
        ceiling = 0.0,
        carrier = "",
        endsAt = 0,
        driverId = "",
        attributes = mutableMapOf<String, String>(),
        startsAt = 0,
        expiresAt = 0,
        status = TieredScorecardNoticeStatus.PLANNED,
    )
}
