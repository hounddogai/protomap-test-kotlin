package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict availability notice is in its life cycle. */
enum class StrictAvailabilityNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One strict availability notice, with what the planners need to act on it. */
data class StrictAvailabilityNotice(
    var code: String,
    var title: String,
    var channel: String,
    var ceiling: Double,
    var sequence: Int,
    var category: String,
    var status: StrictAvailabilityNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAvailabilityNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAvailabilityNoticeStatus): Boolean {
        when (status) {
            StrictAvailabilityNoticeStatus.CREATED -> {
                when (next) {
                    StrictAvailabilityNoticeStatus.PUBLISHED -> return true
                }
                return false
            }
            StrictAvailabilityNoticeStatus.VALIDATED -> return next == StrictAvailabilityNoticeStatus.PUBLISHED || next == StrictAvailabilityNoticeStatus.RETIRED
            StrictAvailabilityNoticeStatus.PUBLISHED -> {
                when (next) {
                    StrictAvailabilityNoticeStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictAvailabilityNoticeStatus) {
        if (!canMoveTo(next)) {
            error("strict availability notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round((ceiling - sequence.toDouble()) * 100) / 100

    fun snapshot(): StrictAvailabilityNotice {
        return StrictAvailabilityNotice(
            code = code,
            title = title,
            channel = channel,
            ceiling = ceiling,
            sequence = sequence,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict availability notice in status created. */
fun newStrictAvailabilityNotice(code: String, title: String): StrictAvailabilityNotice {
    return StrictAvailabilityNotice(
        code = code,
        title = title,
        channel = "",
        ceiling = 0.0,
        sequence = 0,
        category = "",
        status = StrictAvailabilityNoticeStatus.CREATED,
    )
}
