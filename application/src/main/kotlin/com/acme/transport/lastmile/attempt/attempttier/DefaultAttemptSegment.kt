package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default attempt segment is in its life cycle. */
enum class DefaultAttemptSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One default attempt segment, with what the planners need to act on it. */
data class DefaultAttemptSegment(
    var code: String,
    var label: String,
    var region: String,
    var sequence: Int,
    var dock: String,
    var margin: Double,
    var status: DefaultAttemptSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAttemptSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAttemptSegmentStatus): Boolean {
        when (status) {
            DefaultAttemptSegmentStatus.REQUESTED -> {
                when (next) {
                    DefaultAttemptSegmentStatus.REVOKED -> return true
                }
                return false
            }
            DefaultAttemptSegmentStatus.GRANTED -> {
                when (next) {
                    DefaultAttemptSegmentStatus.REVOKED, DefaultAttemptSegmentStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultAttemptSegmentStatus.REVOKED -> return next == DefaultAttemptSegmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultAttemptSegmentStatus) {
        if (!canMoveTo(next)) {
            error("default attempt segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((margin - sequence.toDouble()) * 100) / 100

    fun snapshot(): DefaultAttemptSegment {
        return DefaultAttemptSegment(
            code = code,
            label = label,
            region = region,
            sequence = sequence,
            dock = dock,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default attempt segment in status requested. */
fun newDefaultAttemptSegment(code: String, label: String): DefaultAttemptSegment {
    return DefaultAttemptSegment(
        code = code,
        label = label,
        region = "",
        sequence = 0,
        dock = "",
        margin = 0.0,
        status = DefaultAttemptSegmentStatus.REQUESTED,
    )
}
