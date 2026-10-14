package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance exception is in its life cycle. */
enum class ClearanceExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One clearance exception, with what the planners need to act on it. */
data class ClearanceException(
    var code: String,
    var name: String,
    var threshold: Double,
    var createdAt: Int,
    var score: Double,
    var expiresAt: Int,
    var status: ClearanceExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceExceptionStatus): Boolean {
        when (status) {
            ClearanceExceptionStatus.IDLE -> return next == ClearanceExceptionStatus.READY
            ClearanceExceptionStatus.WARMING -> {
                when (next) {
                    ClearanceExceptionStatus.STOPPED -> return true
                }
                return false
            }
            ClearanceExceptionStatus.READY -> return next == ClearanceExceptionStatus.DRAINING || next == ClearanceExceptionStatus.STOPPED
            ClearanceExceptionStatus.DRAINING -> return next == ClearanceExceptionStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ClearanceExceptionStatus) {
        if (!canMoveTo(next)) {
            error("clearance exception $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"clearance exception"} $code"

    fun weightedLoad(): Double = round((expiresAt.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): ClearanceException {
        return ClearanceException(
            code = code,
            name = name,
            threshold = threshold,
            createdAt = createdAt,
            score = score,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a clearance exception in status idle. */
fun newClearanceException(code: String, name: String): ClearanceException {
    return ClearanceException(
        code = code,
        name = name,
        threshold = 0.0,
        createdAt = 0,
        score = 0.0,
        expiresAt = 0,
        status = ClearanceExceptionStatus.IDLE,
    )
}
