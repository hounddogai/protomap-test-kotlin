package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender exception is in its life cycle. */
enum class TenderExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tender exception. Its code is unique within a tenant. */
data class TenderException(
    var code: String,
    var name: String,
    var stopCount: Int,
    var priority: Int,
    var rate: Double,
    var odometer: Int,
    var segment: String,
    var status: TenderExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderExceptionStatus): Boolean {
        when (status) {
            TenderExceptionStatus.IDLE -> {
                when (next) {
                    TenderExceptionStatus.DRAINING -> return true
                }
                return false
            }
            TenderExceptionStatus.WARMING -> {
                when (next) {
                    TenderExceptionStatus.READY, TenderExceptionStatus.STOPPED -> return true
                }
                return false
            }
            TenderExceptionStatus.READY -> {
                when (next) {
                    TenderExceptionStatus.DRAINING, TenderExceptionStatus.STOPPED -> return true
                }
                return false
            }
            TenderExceptionStatus.DRAINING -> return next == TenderExceptionStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TenderExceptionStatus) {
        if (!canMoveTo(next)) {
            error("tender exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tender exception"} $code"

    fun effectiveExposure(): Double = round((priority.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): TenderException {
        return TenderException(
            code = code,
            name = name,
            stopCount = stopCount,
            priority = priority,
            rate = rate,
            odometer = odometer,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tender exception in status idle. */
fun newTenderException(code: String, name: String): TenderException {
    return TenderException(
        code = code,
        name = name,
        stopCount = 0,
        priority = 0,
        rate = 0.0,
        odometer = 0,
        segment = "",
        status = TenderExceptionStatus.IDLE,
    )
}
