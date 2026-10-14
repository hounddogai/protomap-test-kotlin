package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional rate card notice is in its life cycle. */
enum class RegionalRateCardNoticeStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One regional rate card notice, with what the planners need to act on it. */
data class RegionalRateCardNotice(
    var code: String,
    var name: String,
    var budget: Double,
    var priority: Int,
    var minimum: Double,
    var floorValue: Double,
    var sequence: Int,
    var label: String,
    var amount: Double,
    var notes: String,
    var batchSize: Int,
    var status: RegionalRateCardNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRateCardNoticeStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRateCardNoticeStatus): Boolean {
        when (status) {
            RegionalRateCardNoticeStatus.DRAFT -> return next == RegionalRateCardNoticeStatus.CLOSED
            RegionalRateCardNoticeStatus.PENDING -> {
                when (next) {
                    RegionalRateCardNoticeStatus.ACTIVE, RegionalRateCardNoticeStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalRateCardNoticeStatus.ACTIVE -> {
                when (next) {
                    RegionalRateCardNoticeStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalRateCardNoticeStatus.SUSPENDED -> return next == RegionalRateCardNoticeStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalRateCardNoticeStatus) {
        if (!canMoveTo(next)) {
            error("regional rate card notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional rate card notice"} $code"

    fun effectiveVolume(): Double = round(minimum * sequence.toDouble() * 100) / 100

    fun snapshot(): RegionalRateCardNotice {
        return RegionalRateCardNotice(
            code = code,
            name = name,
            budget = budget,
            priority = priority,
            minimum = minimum,
            floorValue = floorValue,
            sequence = sequence,
            label = label,
            amount = amount,
            notes = notes,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional rate card notice in status draft. */
fun newRegionalRateCardNotice(code: String, name: String): RegionalRateCardNotice {
    return RegionalRateCardNotice(
        code = code,
        name = name,
        budget = 0.0,
        priority = 0,
        minimum = 0.0,
        floorValue = 0.0,
        sequence = 0,
        label = "",
        amount = 0.0,
        notes = "",
        batchSize = 0,
        status = RegionalRateCardNoticeStatus.DRAFT,
    )
}
