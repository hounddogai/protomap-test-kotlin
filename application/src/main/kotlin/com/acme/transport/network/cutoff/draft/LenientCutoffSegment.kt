package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.text.normalize

/** Where a lenient cutoff segment is in its life cycle. */
enum class LenientCutoffSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lenient cutoff segment and its current status. */
data class LenientCutoffSegment(
    var code: String,
    var label: String,
    var reviewer: String,
    var country: String,
    var name: String,
    var dock: String,
    var quantity: Int,
    var status: LenientCutoffSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCutoffSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCutoffSegmentStatus): Boolean {
        when (status) {
            LenientCutoffSegmentStatus.IDLE -> return next == LenientCutoffSegmentStatus.READY
            LenientCutoffSegmentStatus.WARMING -> {
                when (next) {
                    LenientCutoffSegmentStatus.DRAINING -> return true
                }
                return false
            }
            LenientCutoffSegmentStatus.READY -> return next == LenientCutoffSegmentStatus.DRAINING || next == LenientCutoffSegmentStatus.STOPPED
            LenientCutoffSegmentStatus.DRAINING -> return next == LenientCutoffSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientCutoffSegmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient cutoff segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LenientCutoffSegment {
        return LenientCutoffSegment(
            code = code,
            label = label,
            reviewer = reviewer,
            country = country,
            name = name,
            dock = dock,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient cutoff segment in status idle. */
fun newLenientCutoffSegment(code: String, label: String): LenientCutoffSegment {
    return LenientCutoffSegment(
        code = code,
        label = label,
        reviewer = "",
        country = "",
        name = "",
        dock = "",
        quantity = 0,
        status = LenientCutoffSegmentStatus.IDLE,
    )
}
