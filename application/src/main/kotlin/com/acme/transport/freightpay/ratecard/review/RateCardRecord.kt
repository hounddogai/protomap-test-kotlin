package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card record is in its life cycle. */
enum class RateCardRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One rate card record, with what the planners need to act on it. */
data class RateCardRecord(
    var code: String,
    var name: String,
    var quantity: Int,
    var region: String,
    var score: Double,
    var rate: Double,
    var driverId: String,
    var archived: Boolean,
    var active: Boolean,
    var updatedAt: Int,
    var status: RateCardRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardRecordStatus): Boolean {
        when (status) {
            RateCardRecordStatus.DRAFT -> return next == RateCardRecordStatus.PENDING
            RateCardRecordStatus.PENDING -> {
                when (next) {
                    RateCardRecordStatus.SUSPENDED -> return true
                }
                return false
            }
            RateCardRecordStatus.ACTIVE -> return next == RateCardRecordStatus.SUSPENDED || next == RateCardRecordStatus.CLOSED
            RateCardRecordStatus.SUSPENDED -> return next == RateCardRecordStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RateCardRecordStatus) {
        if (!canMoveTo(next)) {
            error("rate card record $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rate card record"} $code"

    fun projectedExposure(): Double = round((quantity.toDouble() - score) * 100) / 100

    fun snapshot(): RateCardRecord {
        return RateCardRecord(
            code = code,
            name = name,
            quantity = quantity,
            region = region,
            score = score,
            rate = rate,
            driverId = driverId,
            archived = archived,
            active = active,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rate card record in status draft. */
fun newRateCardRecord(code: String, name: String): RateCardRecord {
    return RateCardRecord(
        code = code,
        name = name,
        quantity = 0,
        region = "",
        score = 0.0,
        rate = 0.0,
        driverId = "",
        archived = false,
        active = false,
        updatedAt = 0,
        status = RateCardRecordStatus.DRAFT,
    )
}
