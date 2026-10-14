package com.acme.transport.safety.coaching.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict coaching exception records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a strict coaching exception is in its life cycle. */
enum class StrictCoachingExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict coaching exception as the coaching team keeps it. */
data class StrictCoachingException(
    var code: String,
    var label: String,
    var phone: String,
    var stopCount: Int,
    var source: String,
    var locale: String,
    var fuelLiters: Double,
    var notes: String,
    var status: StrictCoachingExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCoachingExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCoachingExceptionStatus): Boolean {
        when (status) {
            StrictCoachingExceptionStatus.NEW -> {
                when (next) {
                    StrictCoachingExceptionStatus.QUEUED -> return true
                }
                return false
            }
            StrictCoachingExceptionStatus.QUEUED -> {
                when (next) {
                    StrictCoachingExceptionStatus.RUNNING, StrictCoachingExceptionStatus.DONE -> return true
                }
                return false
            }
            StrictCoachingExceptionStatus.RUNNING -> return next == StrictCoachingExceptionStatus.DONE || next == StrictCoachingExceptionStatus.FAILED
            StrictCoachingExceptionStatus.DONE -> {
                when (next) {
                    StrictCoachingExceptionStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictCoachingExceptionStatus) {
        if (!canMoveTo(next)) {
            error("strict coaching exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveLoad(): Double = round((stopCount.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): StrictCoachingException {
        return StrictCoachingException(
            code = code,
            label = label,
            phone = phone,
            stopCount = stopCount,
            source = source,
            locale = locale,
            fuelLiters = fuelLiters,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a strict coaching exception in status new. */
fun newStrictCoachingException(code: String, label: String): StrictCoachingException {
    return StrictCoachingException(
        code = code,
        label = label,
        phone = "",
        stopCount = 0,
        source = "",
        locale = "",
        fuelLiters = 0.0,
        notes = "",
        status = StrictCoachingExceptionStatus.NEW,
    )
}
