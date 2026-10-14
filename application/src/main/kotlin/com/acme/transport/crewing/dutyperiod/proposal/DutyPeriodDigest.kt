package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period digest is in its life cycle. */
enum class DutyPeriodDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a duty period digest and its current status. */
data class DutyPeriodDigest(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var graceDays: Int,
    var margin: Double,
    var status: DutyPeriodDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodDigestStatus): Boolean {
        when (status) {
            DutyPeriodDigestStatus.IDLE -> return next == DutyPeriodDigestStatus.READY || next == DutyPeriodDigestStatus.DRAINING
            DutyPeriodDigestStatus.WARMING -> return next == DutyPeriodDigestStatus.STOPPED
            DutyPeriodDigestStatus.READY -> {
                when (next) {
                    DutyPeriodDigestStatus.DRAINING, DutyPeriodDigestStatus.STOPPED -> return true
                }
                return false
            }
            DutyPeriodDigestStatus.DRAINING -> {
                when (next) {
                    DutyPeriodDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodDigestStatus) {
        if (!canMoveTo(next)) {
            error("duty period digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((graceDays.toDouble() - margin) * 100) / 100

    fun snapshot(): DutyPeriodDigest {
        return DutyPeriodDigest(
            code = code,
            title = title,
            tags = tags,
            graceDays = graceDays,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a duty period digest in status idle. */
fun newDutyPeriodDigest(code: String, title: String): DutyPeriodDigest {
    return DutyPeriodDigest(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        graceDays = 0,
        margin = 0.0,
        status = DutyPeriodDigestStatus.IDLE,
    )
}
