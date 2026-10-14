package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster notice is in its life cycle. */
enum class RosterNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A roster notice as the roster team keeps it. */
data class RosterNotice(
    var code: String,
    var title: String,
    var floorValue: Double,
    var channel: String,
    var updatedAt: Int,
    var margin: Double,
    var currency: String,
    var tags: MutableList<String>,
    var active: Boolean,
    var carrier: String,
    var status: RosterNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterNoticeStatus): Boolean {
        when (status) {
            RosterNoticeStatus.CREATED -> return next == RosterNoticeStatus.PUBLISHED
            RosterNoticeStatus.VALIDATED -> return next == RosterNoticeStatus.PUBLISHED
            RosterNoticeStatus.PUBLISHED -> return next == RosterNoticeStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RosterNoticeStatus) {
        if (!canMoveTo(next)) {
            error("roster notice $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"roster notice"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((updatedAt.toDouble() - floorValue) * 100) / 100

    fun snapshot(): RosterNotice {
        return RosterNotice(
            code = code,
            title = title,
            floorValue = floorValue,
            channel = channel,
            updatedAt = updatedAt,
            margin = margin,
            currency = currency,
            tags = tags,
            active = active,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a roster notice in status created. */
fun newRosterNotice(code: String, title: String): RosterNotice {
    return RosterNotice(
        code = code,
        title = title,
        floorValue = 0.0,
        channel = "",
        updatedAt = 0,
        margin = 0.0,
        currency = "",
        tags = mutableListOf<String>(),
        active = false,
        carrier = "",
        status = RosterNoticeStatus.CREATED,
    )
}
