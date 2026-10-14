package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling accessorial batch is in its life cycle. */
enum class RollingAccessorialBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling accessorial batch as the accessorial team keeps it. */
data class RollingAccessorialBatch(
    var code: String,
    var name: String,
    var retries: Int,
    var score: Double,
    var driverId: String,
    var quantity: Int,
    var summary: String,
    var status: RollingAccessorialBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAccessorialBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAccessorialBatchStatus): Boolean {
        when (status) {
            RollingAccessorialBatchStatus.REQUESTED -> return next == RollingAccessorialBatchStatus.REVOKED || next == RollingAccessorialBatchStatus.EXPIRED
            RollingAccessorialBatchStatus.GRANTED -> {
                when (next) {
                    RollingAccessorialBatchStatus.REVOKED, RollingAccessorialBatchStatus.EXPIRED -> return true
                }
                return false
            }
            RollingAccessorialBatchStatus.REVOKED -> {
                when (next) {
                    RollingAccessorialBatchStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingAccessorialBatchStatus) {
        if (!canMoveTo(next)) {
            error("rolling accessorial batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedValue(): Double = round((quantity.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): RollingAccessorialBatch {
        return RollingAccessorialBatch(
            code = code,
            name = name,
            retries = retries,
            score = score,
            driverId = driverId,
            quantity = quantity,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling accessorial batch in status requested. */
fun newRollingAccessorialBatch(code: String, name: String): RollingAccessorialBatch {
    return RollingAccessorialBatch(
        code = code,
        name = name,
        retries = 0,
        score = 0.0,
        driverId = "",
        quantity = 0,
        summary = "",
        status = RollingAccessorialBatchStatus.REQUESTED,
    )
}
