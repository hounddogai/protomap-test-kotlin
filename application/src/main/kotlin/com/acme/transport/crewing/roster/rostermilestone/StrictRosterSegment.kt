package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.text.normalize

/** Where a strict roster segment is in its life cycle. */
enum class StrictRosterSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict roster segment as the roster team keeps it. */
data class StrictRosterSegment(
    var code: String,
    var label: String,
    var source: String,
    var region: String,
    var pallets: Int,
    var summary: String,
    var ownerId: String,
    var approved: Boolean,
    var status: StrictRosterSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRosterSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRosterSegmentStatus): Boolean {
        when (status) {
            StrictRosterSegmentStatus.NEW -> {
                when (next) {
                    StrictRosterSegmentStatus.QUEUED, StrictRosterSegmentStatus.FAILED -> return true
                }
                return false
            }
            StrictRosterSegmentStatus.QUEUED -> {
                when (next) {
                    StrictRosterSegmentStatus.FAILED -> return true
                }
                return false
            }
            StrictRosterSegmentStatus.RUNNING -> {
                when (next) {
                    StrictRosterSegmentStatus.DONE, StrictRosterSegmentStatus.FAILED -> return true
                }
                return false
            }
            StrictRosterSegmentStatus.DONE -> return next == StrictRosterSegmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictRosterSegmentStatus) {
        if (!canMoveTo(next)) {
            error("strict roster segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict roster segment"} $code"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): StrictRosterSegment {
        return StrictRosterSegment(
            code = code,
            label = label,
            source = source,
            region = region,
            pallets = pallets,
            summary = summary,
            ownerId = ownerId,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a strict roster segment in status new. */
fun newStrictRosterSegment(code: String, label: String): StrictRosterSegment {
    return StrictRosterSegment(
        code = code,
        label = label,
        source = "",
        region = "",
        pallets = 0,
        summary = "",
        ownerId = "",
        approved = false,
        status = StrictRosterSegmentStatus.NEW,
    )
}
