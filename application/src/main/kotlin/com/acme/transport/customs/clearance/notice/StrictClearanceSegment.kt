package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict clearance segment is in its life cycle. */
enum class StrictClearanceSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a strict clearance segment and its current status. */
data class StrictClearanceSegment(
    var code: String,
    var title: String,
    var odometer: Int,
    var locale: String,
    var attributes: MutableMap<String, String>,
    var archived: Boolean,
    var retries: Int,
    var description: String,
    var carrier: String,
    var status: StrictClearanceSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictClearanceSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictClearanceSegmentStatus): Boolean {
        when (status) {
            StrictClearanceSegmentStatus.IDLE -> {
                when (next) {
                    StrictClearanceSegmentStatus.WARMING -> return true
                }
                return false
            }
            StrictClearanceSegmentStatus.WARMING -> {
                when (next) {
                    StrictClearanceSegmentStatus.READY, StrictClearanceSegmentStatus.STOPPED -> return true
                }
                return false
            }
            StrictClearanceSegmentStatus.READY -> return next == StrictClearanceSegmentStatus.DRAINING || next == StrictClearanceSegmentStatus.STOPPED
            StrictClearanceSegmentStatus.DRAINING -> {
                when (next) {
                    StrictClearanceSegmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictClearanceSegmentStatus) {
        if (!canMoveTo(next)) {
            error("strict clearance segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict clearance segment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((retries.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): StrictClearanceSegment {
        return StrictClearanceSegment(
            code = code,
            title = title,
            odometer = odometer,
            locale = locale,
            attributes = attributes,
            archived = archived,
            retries = retries,
            description = description,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict clearance segment in status idle. */
fun newStrictClearanceSegment(code: String, title: String): StrictClearanceSegment {
    return StrictClearanceSegment(
        code = code,
        title = title,
        odometer = 0,
        locale = "",
        attributes = mutableMapOf<String, String>(),
        archived = false,
        retries = 0,
        description = "",
        carrier = "",
        status = StrictClearanceSegmentStatus.IDLE,
    )
}
