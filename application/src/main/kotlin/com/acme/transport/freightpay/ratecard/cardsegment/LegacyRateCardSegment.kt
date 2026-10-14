package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy rate card segment records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a legacy rate card segment is in its life cycle. */
enum class LegacyRateCardSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy rate card segment. Its code is unique within a tenant. */
data class LegacyRateCardSegment(
    var code: String,
    var label: String,
    var ownerId: String,
    var segment: String,
    var maximum: Double,
    var rate: Double,
    var driverId: String,
    var phone: String,
    var lane: String,
    var status: LegacyRateCardSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRateCardSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRateCardSegmentStatus): Boolean {
        when (status) {
            LegacyRateCardSegmentStatus.NEW -> {
                when (next) {
                    LegacyRateCardSegmentStatus.DONE, LegacyRateCardSegmentStatus.FAILED -> return true
                }
                return false
            }
            LegacyRateCardSegmentStatus.QUEUED -> return next == LegacyRateCardSegmentStatus.DONE
            LegacyRateCardSegmentStatus.RUNNING -> {
                when (next) {
                    LegacyRateCardSegmentStatus.FAILED -> return true
                }
                return false
            }
            LegacyRateCardSegmentStatus.DONE -> return next == LegacyRateCardSegmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyRateCardSegmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy rate card segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netValue(): Double = round((maximum - rate) * 100) / 100

    fun snapshot(): LegacyRateCardSegment {
        return LegacyRateCardSegment(
            code = code,
            label = label,
            ownerId = ownerId,
            segment = segment,
            maximum = maximum,
            rate = rate,
            driverId = driverId,
            phone = phone,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy rate card segment in status new. */
fun newLegacyRateCardSegment(code: String, label: String): LegacyRateCardSegment {
    return LegacyRateCardSegment(
        code = code,
        label = label,
        ownerId = "",
        segment = "",
        maximum = 0.0,
        rate = 0.0,
        driverId = "",
        phone = "",
        lane = "",
        status = LegacyRateCardSegmentStatus.NEW,
    )
}
