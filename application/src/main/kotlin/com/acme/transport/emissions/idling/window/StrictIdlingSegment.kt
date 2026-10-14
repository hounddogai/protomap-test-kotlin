package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.text.normalize

/** Where a strict idling segment is in its life cycle. */
enum class StrictIdlingSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One strict idling segment, with what the planners need to act on it. */
data class StrictIdlingSegment(
    var code: String,
    var title: String,
    var capacity: Int,
    var tags: MutableList<String>,
    var currency: String,
    var status: StrictIdlingSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictIdlingSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictIdlingSegmentStatus): Boolean {
        when (status) {
            StrictIdlingSegmentStatus.IDLE -> return next == StrictIdlingSegmentStatus.STOPPED
            StrictIdlingSegmentStatus.WARMING -> return next == StrictIdlingSegmentStatus.DRAINING || next == StrictIdlingSegmentStatus.STOPPED
            StrictIdlingSegmentStatus.READY -> {
                when (next) {
                    StrictIdlingSegmentStatus.STOPPED -> return true
                }
                return false
            }
            StrictIdlingSegmentStatus.DRAINING -> return next == StrictIdlingSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictIdlingSegmentStatus) {
        if (!canMoveTo(next)) {
            error("strict idling segment $code cannot move from $status to $next")
        }
        status = next
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

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): StrictIdlingSegment {
        return StrictIdlingSegment(
            code = code,
            title = title,
            capacity = capacity,
            tags = tags,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict idling segment in status idle. */
fun newStrictIdlingSegment(code: String, title: String): StrictIdlingSegment {
    return StrictIdlingSegment(
        code = code,
        title = title,
        capacity = 0,
        tags = mutableListOf<String>(),
        currency = "",
        status = StrictIdlingSegmentStatus.IDLE,
    )
}
