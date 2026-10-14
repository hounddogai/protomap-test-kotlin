package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rate card snapshot records of freightpay's rate card work, and the rules, stores, and calculations
// around them.

/** Where a rate card snapshot is in its life cycle. */
enum class RateCardSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rate card snapshot. Its code is unique within a tenant. */
data class RateCardSnapshot(
    var code: String,
    var label: String,
    var floorValue: Double,
    var phone: String,
    var summary: String,
    var amount: Double,
    var currency: String,
    var visible: Boolean,
    var reviewer: String,
    var channel: String,
    var status: RateCardSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardSnapshotStatus): Boolean {
        when (status) {
            RateCardSnapshotStatus.CREATED -> return next == RateCardSnapshotStatus.PUBLISHED
            RateCardSnapshotStatus.VALIDATED -> {
                when (next) {
                    RateCardSnapshotStatus.PUBLISHED, RateCardSnapshotStatus.RETIRED -> return true
                }
                return false
            }
            RateCardSnapshotStatus.PUBLISHED -> return next == RateCardSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RateCardSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("rate card snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedTotal(): Double = round((amount - floorValue) * 100) / 100

    fun snapshot(): RateCardSnapshot {
        return RateCardSnapshot(
            code = code,
            label = label,
            floorValue = floorValue,
            phone = phone,
            summary = summary,
            amount = amount,
            currency = currency,
            visible = visible,
            reviewer = reviewer,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rate card snapshot in status created. */
fun newRateCardSnapshot(code: String, label: String): RateCardSnapshot {
    return RateCardSnapshot(
        code = code,
        label = label,
        floorValue = 0.0,
        phone = "",
        summary = "",
        amount = 0.0,
        currency = "",
        visible = false,
        reviewer = "",
        channel = "",
        status = RateCardSnapshotStatus.CREATED,
    )
}
