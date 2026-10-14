package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global rate card record is in its life cycle. */
enum class GlobalRateCardRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global rate card record. Its code is unique within a tenant. */
data class GlobalRateCardRecord(
    var code: String,
    var label: String,
    var carrier: String,
    var visible: Boolean,
    var expiresAt: Int,
    var externalRef: String,
    var checksum: String,
    var sequence: Int,
    var cost: Double,
    var trailerId: String,
    var status: GlobalRateCardRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRateCardRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRateCardRecordStatus): Boolean {
        when (status) {
            GlobalRateCardRecordStatus.DRAFT -> {
                when (next) {
                    GlobalRateCardRecordStatus.PENDING, GlobalRateCardRecordStatus.ACTIVE -> return true
                }
                return false
            }
            GlobalRateCardRecordStatus.PENDING -> return next == GlobalRateCardRecordStatus.SUSPENDED
            GlobalRateCardRecordStatus.ACTIVE -> {
                when (next) {
                    GlobalRateCardRecordStatus.SUSPENDED, GlobalRateCardRecordStatus.CLOSED -> return true
                }
                return false
            }
            GlobalRateCardRecordStatus.SUSPENDED -> return next == GlobalRateCardRecordStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: GlobalRateCardRecordStatus) {
        if (!canMoveTo(next)) {
            error("global rate card record $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round((cost + sequence.toDouble()) * 100) / 100

    fun snapshot(): GlobalRateCardRecord {
        return GlobalRateCardRecord(
            code = code,
            label = label,
            carrier = carrier,
            visible = visible,
            expiresAt = expiresAt,
            externalRef = externalRef,
            checksum = checksum,
            sequence = sequence,
            cost = cost,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global rate card record in status draft. */
fun newGlobalRateCardRecord(code: String, label: String): GlobalRateCardRecord {
    return GlobalRateCardRecord(
        code = code,
        label = label,
        carrier = "",
        visible = false,
        expiresAt = 0,
        externalRef = "",
        checksum = "",
        sequence = 0,
        cost = 0.0,
        trailerId = "",
        status = GlobalRateCardRecordStatus.DRAFT,
    )
}
