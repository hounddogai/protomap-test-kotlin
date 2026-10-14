package com.acme.transport.customs.broker.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker notice is in its life cycle. */
enum class BrokerNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A broker notice as the broker team keeps it. */
data class BrokerNotice(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var quantity: Int,
    var durationMinutes: Int,
    var expiresAt: Int,
    var status: BrokerNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerNoticeStatus): Boolean {
        when (status) {
            BrokerNoticeStatus.CREATED -> return next == BrokerNoticeStatus.PUBLISHED || next == BrokerNoticeStatus.RETIRED
            BrokerNoticeStatus.VALIDATED -> {
                when (next) {
                    BrokerNoticeStatus.PUBLISHED, BrokerNoticeStatus.RETIRED -> return true
                }
                return false
            }
            BrokerNoticeStatus.PUBLISHED -> {
                when (next) {
                    BrokerNoticeStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerNoticeStatus) {
        if (!canMoveTo(next)) {
            error("broker notice $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netValue(): Double = round(durationMinutes.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): BrokerNotice {
        return BrokerNotice(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            quantity = quantity,
            durationMinutes = durationMinutes,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a broker notice in status created. */
fun newBrokerNotice(code: String, label: String): BrokerNotice {
    return BrokerNotice(
        code = code,
        label = label,
        fuelLiters = 0.0,
        quantity = 0,
        durationMinutes = 0,
        expiresAt = 0,
        status = BrokerNoticeStatus.CREATED,
    )
}
