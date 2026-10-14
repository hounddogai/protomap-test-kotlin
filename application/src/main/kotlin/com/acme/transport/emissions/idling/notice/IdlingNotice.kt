package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The idling notice records of emissions's idling work, and the rules, stores, and calculations around them.

/** Where a idling notice is in its life cycle. */
enum class IdlingNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a idling notice and its current status. */
data class IdlingNotice(
    var code: String,
    var title: String,
    var ownerId: String,
    var floorValue: Double,
    var startsAt: Int,
    var carrier: String,
    var graceDays: Int,
    var channel: String,
    var revision: Int,
    var status: IdlingNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingNoticeStatus): Boolean {
        when (status) {
            IdlingNoticeStatus.CREATED -> return next == IdlingNoticeStatus.VALIDATED || next == IdlingNoticeStatus.PUBLISHED
            IdlingNoticeStatus.VALIDATED -> return next == IdlingNoticeStatus.PUBLISHED
            IdlingNoticeStatus.PUBLISHED -> {
                when (next) {
                    IdlingNoticeStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingNoticeStatus) {
        if (!canMoveTo(next)) {
            error("idling notice $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"idling notice"} $code"

    fun adjustedTotal(): Double = round(floorValue * graceDays.toDouble() * 100) / 100

    fun snapshot(): IdlingNotice {
        return IdlingNotice(
            code = code,
            title = title,
            ownerId = ownerId,
            floorValue = floorValue,
            startsAt = startsAt,
            carrier = carrier,
            graceDays = graceDays,
            channel = channel,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a idling notice in status created. */
fun newIdlingNotice(code: String, title: String): IdlingNotice {
    return IdlingNotice(
        code = code,
        title = title,
        ownerId = "",
        floorValue = 0.0,
        startsAt = 0,
        carrier = "",
        graceDays = 0,
        channel = "",
        revision = 0,
        status = IdlingNoticeStatus.CREATED,
    )
}
