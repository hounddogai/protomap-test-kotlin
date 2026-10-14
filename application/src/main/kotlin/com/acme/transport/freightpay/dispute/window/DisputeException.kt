package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute exception is in its life cycle. */
enum class DisputeExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a dispute exception and its current status. */
data class DisputeException(
    var code: String,
    var label: String,
    var windowDays: Int,
    var createdAt: Int,
    var pallets: Int,
    var expiresAt: Int,
    var archived: Boolean,
    var status: DisputeExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeExceptionStatus): Boolean {
        when (status) {
            DisputeExceptionStatus.IDLE -> return next == DisputeExceptionStatus.READY || next == DisputeExceptionStatus.DRAINING
            DisputeExceptionStatus.WARMING -> {
                when (next) {
                    DisputeExceptionStatus.READY, DisputeExceptionStatus.STOPPED -> return true
                }
                return false
            }
            DisputeExceptionStatus.READY -> return next == DisputeExceptionStatus.DRAINING || next == DisputeExceptionStatus.STOPPED
            DisputeExceptionStatus.DRAINING -> return next == DisputeExceptionStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DisputeExceptionStatus) {
        if (!canMoveTo(next)) {
            error("dispute exception $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round((createdAt.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): DisputeException {
        return DisputeException(
            code = code,
            label = label,
            windowDays = windowDays,
            createdAt = createdAt,
            pallets = pallets,
            expiresAt = expiresAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dispute exception in status idle. */
fun newDisputeException(code: String, label: String): DisputeException {
    return DisputeException(
        code = code,
        label = label,
        windowDays = 0,
        createdAt = 0,
        pallets = 0,
        expiresAt = 0,
        archived = false,
        status = DisputeExceptionStatus.IDLE,
    )
}
