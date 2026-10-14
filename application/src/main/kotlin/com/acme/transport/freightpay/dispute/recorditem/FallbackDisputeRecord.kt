package com.acme.transport.freightpay.dispute.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback dispute record records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a fallback dispute record is in its life cycle. */
enum class FallbackDisputeRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a fallback dispute record and its current status. */
data class FallbackDisputeRecord(
    var code: String,
    var name: String,
    var visible: Boolean,
    var locale: String,
    var channel: String,
    var floorValue: Double,
    var etaMinutes: Int,
    var minimum: Double,
    var createdAt: Int,
    var timezone: String,
    var status: FallbackDisputeRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDisputeRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDisputeRecordStatus): Boolean {
        when (status) {
            FallbackDisputeRecordStatus.PROPOSED -> {
                when (next) {
                    FallbackDisputeRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackDisputeRecordStatus.APPROVED -> {
                when (next) {
                    FallbackDisputeRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackDisputeRecordStatus.REJECTED -> return next == FallbackDisputeRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackDisputeRecordStatus) {
        if (!canMoveTo(next)) {
            error("fallback dispute record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netVolume(): Double = round((createdAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): FallbackDisputeRecord {
        return FallbackDisputeRecord(
            code = code,
            name = name,
            visible = visible,
            locale = locale,
            channel = channel,
            floorValue = floorValue,
            etaMinutes = etaMinutes,
            minimum = minimum,
            createdAt = createdAt,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback dispute record in status proposed. */
fun newFallbackDisputeRecord(code: String, name: String): FallbackDisputeRecord {
    return FallbackDisputeRecord(
        code = code,
        name = name,
        visible = false,
        locale = "",
        channel = "",
        floorValue = 0.0,
        etaMinutes = 0,
        minimum = 0.0,
        createdAt = 0,
        timezone = "",
        status = FallbackDisputeRecordStatus.PROPOSED,
    )
}
