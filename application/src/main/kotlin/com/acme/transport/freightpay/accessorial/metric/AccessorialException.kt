package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial exception is in its life cycle. */
enum class AccessorialExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One accessorial exception, with what the planners need to act on it. */
data class AccessorialException(
    var code: String,
    var label: String,
    var priority: Int,
    var lane: String,
    var quantity: Int,
    var discount: Double,
    var status: AccessorialExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialExceptionStatus): Boolean {
        when (status) {
            AccessorialExceptionStatus.NEW -> return next == AccessorialExceptionStatus.QUEUED
            AccessorialExceptionStatus.QUEUED -> return next == AccessorialExceptionStatus.DONE
            AccessorialExceptionStatus.RUNNING -> {
                when (next) {
                    AccessorialExceptionStatus.DONE, AccessorialExceptionStatus.FAILED -> return true
                }
                return false
            }
            AccessorialExceptionStatus.DONE -> {
                when (next) {
                    AccessorialExceptionStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialExceptionStatus) {
        if (!canMoveTo(next)) {
            error("accessorial exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"accessorial exception"} $code"

    fun weightedLoad(): Double = round((priority.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): AccessorialException {
        return AccessorialException(
            code = code,
            label = label,
            priority = priority,
            lane = lane,
            quantity = quantity,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a accessorial exception in status new. */
fun newAccessorialException(code: String, label: String): AccessorialException {
    return AccessorialException(
        code = code,
        label = label,
        priority = 0,
        lane = "",
        quantity = 0,
        discount = 0.0,
        status = AccessorialExceptionStatus.NEW,
    )
}
