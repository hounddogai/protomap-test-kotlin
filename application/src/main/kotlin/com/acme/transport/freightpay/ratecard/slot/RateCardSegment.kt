package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.text.normalize

/** Where a rate card segment is in its life cycle. */
enum class RateCardSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rate card segment and its current status. */
data class RateCardSegment(
    var code: String,
    var label: String,
    var visible: Boolean,
    var dock: String,
    var pinned: Boolean,
    var archived: Boolean,
    var ownerId: String,
    var budget: Double,
    var status: RateCardSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardSegmentStatus): Boolean {
        when (status) {
            RateCardSegmentStatus.DRAFT -> return next == RateCardSegmentStatus.PENDING || next == RateCardSegmentStatus.CLOSED
            RateCardSegmentStatus.PENDING -> return next == RateCardSegmentStatus.ACTIVE || next == RateCardSegmentStatus.SUSPENDED
            RateCardSegmentStatus.ACTIVE -> return next == RateCardSegmentStatus.SUSPENDED || next == RateCardSegmentStatus.CLOSED
            RateCardSegmentStatus.SUSPENDED -> {
                when (next) {
                    RateCardSegmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardSegmentStatus) {
        if (!canMoveTo(next)) {
            error("rate card segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = budget

    fun snapshot(): RateCardSegment {
        return RateCardSegment(
            code = code,
            label = label,
            visible = visible,
            dock = dock,
            pinned = pinned,
            archived = archived,
            ownerId = ownerId,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rate card segment in status draft. */
fun newRateCardSegment(code: String, label: String): RateCardSegment {
    return RateCardSegment(
        code = code,
        label = label,
        visible = false,
        dock = "",
        pinned = false,
        archived = false,
        ownerId = "",
        budget = 0.0,
        status = RateCardSegmentStatus.DRAFT,
    )
}
