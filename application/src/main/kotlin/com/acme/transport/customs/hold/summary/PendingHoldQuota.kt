package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending hold quota is in its life cycle. */
enum class PendingHoldQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending hold quota as the hold team keeps it. */
data class PendingHoldQuota(
    var code: String,
    var name: String,
    var shiftCode: String,
    var visible: Boolean,
    var threshold: Double,
    var batchSize: Int,
    var attempts: Int,
    var lane: String,
    var discount: Double,
    var channel: String,
    var status: PendingHoldQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHoldQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHoldQuotaStatus): Boolean {
        when (status) {
            PendingHoldQuotaStatus.OPEN -> return next == PendingHoldQuotaStatus.ARCHIVED
            PendingHoldQuotaStatus.IN_REVIEW -> {
                when (next) {
                    PendingHoldQuotaStatus.RESOLVED -> return true
                }
                return false
            }
            PendingHoldQuotaStatus.RESOLVED -> return next == PendingHoldQuotaStatus.REOPENED || next == PendingHoldQuotaStatus.ARCHIVED
            PendingHoldQuotaStatus.REOPENED -> {
                when (next) {
                    PendingHoldQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingHoldQuotaStatus) {
        if (!canMoveTo(next)) {
            error("pending hold quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round(attempts.toDouble() * threshold * 100) / 100

    fun snapshot(): PendingHoldQuota {
        return PendingHoldQuota(
            code = code,
            name = name,
            shiftCode = shiftCode,
            visible = visible,
            threshold = threshold,
            batchSize = batchSize,
            attempts = attempts,
            lane = lane,
            discount = discount,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending hold quota in status open. */
fun newPendingHoldQuota(code: String, name: String): PendingHoldQuota {
    return PendingHoldQuota(
        code = code,
        name = name,
        shiftCode = "",
        visible = false,
        threshold = 0.0,
        batchSize = 0,
        attempts = 0,
        lane = "",
        discount = 0.0,
        channel = "",
        status = PendingHoldQuotaStatus.OPEN,
    )
}
