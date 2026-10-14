package com.acme.transport.crewing.swap.mapping

import com.acme.transport.kit.text.normalize

/** Where a seasonal swap segment is in its life cycle. */
enum class SeasonalSwapSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One seasonal swap segment, with what the planners need to act on it. */
data class SeasonalSwapSegment(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var checksum: String,
    var shiftCode: String,
    var timezone: String,
    var endsAt: Int,
    var status: SeasonalSwapSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalSwapSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalSwapSegmentStatus): Boolean {
        when (status) {
            SeasonalSwapSegmentStatus.PLANNED -> return next == SeasonalSwapSegmentStatus.SCHEDULED || next == SeasonalSwapSegmentStatus.CANCELLED
            SeasonalSwapSegmentStatus.SCHEDULED -> return next == SeasonalSwapSegmentStatus.COMPLETED
            SeasonalSwapSegmentStatus.IN_PROGRESS -> return next == SeasonalSwapSegmentStatus.COMPLETED
            SeasonalSwapSegmentStatus.COMPLETED -> return next == SeasonalSwapSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalSwapSegmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal swap segment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): SeasonalSwapSegment {
        return SeasonalSwapSegment(
            code = code,
            name = name,
            pinned = pinned,
            checksum = checksum,
            shiftCode = shiftCode,
            timezone = timezone,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal swap segment in status planned. */
fun newSeasonalSwapSegment(code: String, name: String): SeasonalSwapSegment {
    return SeasonalSwapSegment(
        code = code,
        name = name,
        pinned = false,
        checksum = "",
        shiftCode = "",
        timezone = "",
        endsAt = 0,
        status = SeasonalSwapSegmentStatus.PLANNED,
    )
}
