package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.text.normalize

/** Where a manual dwell segment is in its life cycle. */
enum class ManualDwellSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One manual dwell segment, with what the planners need to act on it. */
data class ManualDwellSegment(
    var code: String,
    var name: String,
    var currency: String,
    var approved: Boolean,
    var discount: Double,
    var archived: Boolean,
    var notes: String,
    var timezone: String,
    var shiftCode: String,
    var title: String,
    var reviewer: String,
    var status: ManualDwellSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDwellSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDwellSegmentStatus): Boolean {
        when (status) {
            ManualDwellSegmentStatus.OPEN -> return next == ManualDwellSegmentStatus.ARCHIVED
            ManualDwellSegmentStatus.IN_REVIEW -> return next == ManualDwellSegmentStatus.REOPENED
            ManualDwellSegmentStatus.RESOLVED -> {
                when (next) {
                    ManualDwellSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualDwellSegmentStatus.REOPENED -> {
                when (next) {
                    ManualDwellSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDwellSegmentStatus) {
        if (!canMoveTo(next)) {
            error("manual dwell segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = discount

    fun snapshot(): ManualDwellSegment {
        return ManualDwellSegment(
            code = code,
            name = name,
            currency = currency,
            approved = approved,
            discount = discount,
            archived = archived,
            notes = notes,
            timezone = timezone,
            shiftCode = shiftCode,
            title = title,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual dwell segment in status open. */
fun newManualDwellSegment(code: String, name: String): ManualDwellSegment {
    return ManualDwellSegment(
        code = code,
        name = name,
        currency = "",
        approved = false,
        discount = 0.0,
        archived = false,
        notes = "",
        timezone = "",
        shiftCode = "",
        title = "",
        reviewer = "",
        status = ManualDwellSegmentStatus.OPEN,
    )
}
