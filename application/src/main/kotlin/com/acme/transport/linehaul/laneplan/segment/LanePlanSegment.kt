package com.acme.transport.linehaul.laneplan.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lane plan segment records of linehaul's lane plan work, and the rules, stores, and calculations around
// them.

/** Where a lane plan segment is in its life cycle. */
enum class LanePlanSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One lane plan segment, with what the planners need to act on it. */
data class LanePlanSegment(
    var code: String,
    var title: String,
    var maximum: Double,
    var updatedAt: Int,
    var attributes: MutableMap<String, String>,
    var limit: Int,
    var pinned: Boolean,
    var distanceKm: Double,
    var notes: String,
    var rate: Double,
    var status: LanePlanSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanSegmentStatus): Boolean {
        when (status) {
            LanePlanSegmentStatus.NEW -> return next == LanePlanSegmentStatus.QUEUED || next == LanePlanSegmentStatus.FAILED
            LanePlanSegmentStatus.QUEUED -> return next == LanePlanSegmentStatus.RUNNING || next == LanePlanSegmentStatus.FAILED
            LanePlanSegmentStatus.RUNNING -> {
                when (next) {
                    LanePlanSegmentStatus.FAILED -> return true
                }
                return false
            }
            LanePlanSegmentStatus.DONE -> return next == LanePlanSegmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LanePlanSegmentStatus) {
        if (!canMoveTo(next)) {
            error("lane plan segment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round(distanceKm * limit.toDouble() * 100) / 100

    fun snapshot(): LanePlanSegment {
        return LanePlanSegment(
            code = code,
            title = title,
            maximum = maximum,
            updatedAt = updatedAt,
            attributes = attributes,
            limit = limit,
            pinned = pinned,
            distanceKm = distanceKm,
            notes = notes,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lane plan segment in status new. */
fun newLanePlanSegment(code: String, title: String): LanePlanSegment {
    return LanePlanSegment(
        code = code,
        title = title,
        maximum = 0.0,
        updatedAt = 0,
        attributes = mutableMapOf<String, String>(),
        limit = 0,
        pinned = false,
        distanceKm = 0.0,
        notes = "",
        rate = 0.0,
        status = LanePlanSegmentStatus.NEW,
    )
}
