package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.text.normalize

/** Where a capped locker segment is in its life cycle. */
enum class CappedLockerSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped locker segment. Its code is unique within a tenant. */
data class CappedLockerSegment(
    var code: String,
    var label: String,
    var ceiling: Double,
    var segment: String,
    var channel: String,
    var status: CappedLockerSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLockerSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLockerSegmentStatus): Boolean {
        when (status) {
            CappedLockerSegmentStatus.DRAFT -> return next == CappedLockerSegmentStatus.PENDING || next == CappedLockerSegmentStatus.ACTIVE
            CappedLockerSegmentStatus.PENDING -> {
                when (next) {
                    CappedLockerSegmentStatus.CLOSED -> return true
                }
                return false
            }
            CappedLockerSegmentStatus.ACTIVE -> {
                when (next) {
                    CappedLockerSegmentStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedLockerSegmentStatus.SUSPENDED -> {
                when (next) {
                    CappedLockerSegmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedLockerSegmentStatus) {
        if (!canMoveTo(next)) {
            error("capped locker segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = ceiling

    fun snapshot(): CappedLockerSegment {
        return CappedLockerSegment(
            code = code,
            label = label,
            ceiling = ceiling,
            segment = segment,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped locker segment in status draft. */
fun newCappedLockerSegment(code: String, label: String): CappedLockerSegment {
    return CappedLockerSegment(
        code = code,
        label = label,
        ceiling = 0.0,
        segment = "",
        channel = "",
        status = CappedLockerSegmentStatus.DRAFT,
    )
}
