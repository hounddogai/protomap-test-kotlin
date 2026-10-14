package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold batch records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold batch is in its life cycle. */
enum class HoldBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One hold batch, with what the planners need to act on it. */
data class HoldBatch(
    var code: String,
    var title: String,
    var weight: Double,
    var retries: Int,
    var margin: Double,
    var visible: Boolean,
    var expiresAt: Int,
    var ownerId: String,
    var status: HoldBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldBatchStatus): Boolean {
        when (status) {
            HoldBatchStatus.REQUESTED -> return next == HoldBatchStatus.GRANTED || next == HoldBatchStatus.EXPIRED
            HoldBatchStatus.GRANTED -> return next == HoldBatchStatus.REVOKED || next == HoldBatchStatus.EXPIRED
            HoldBatchStatus.REVOKED -> return next == HoldBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: HoldBatchStatus) {
        if (!canMoveTo(next)) {
            error("hold batch $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netTotal(): Double = round(retries.toDouble() * weight * 100) / 100

    fun snapshot(): HoldBatch {
        return HoldBatch(
            code = code,
            title = title,
            weight = weight,
            retries = retries,
            margin = margin,
            visible = visible,
            expiresAt = expiresAt,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hold batch in status requested. */
fun newHoldBatch(code: String, title: String): HoldBatch {
    return HoldBatch(
        code = code,
        title = title,
        weight = 0.0,
        retries = 0,
        margin = 0.0,
        visible = false,
        expiresAt = 0,
        ownerId = "",
        status = HoldBatchStatus.REQUESTED,
    )
}
