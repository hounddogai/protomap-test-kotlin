package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped attempt exception is in its life cycle. */
enum class CappedAttemptExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One capped attempt exception, with what the planners need to act on it. */
data class CappedAttemptException(
    var code: String,
    var name: String,
    var description: String,
    var version: Int,
    var retries: Int,
    var weight: Double,
    var floorValue: Double,
    var status: CappedAttemptExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAttemptExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAttemptExceptionStatus): Boolean {
        when (status) {
            CappedAttemptExceptionStatus.NEW -> {
                when (next) {
                    CappedAttemptExceptionStatus.FAILED -> return true
                }
                return false
            }
            CappedAttemptExceptionStatus.QUEUED -> return next == CappedAttemptExceptionStatus.DONE
            CappedAttemptExceptionStatus.RUNNING -> return next == CappedAttemptExceptionStatus.DONE || next == CappedAttemptExceptionStatus.FAILED
            CappedAttemptExceptionStatus.DONE -> return next == CappedAttemptExceptionStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedAttemptExceptionStatus) {
        if (!canMoveTo(next)) {
            error("capped attempt exception $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round((version.toDouble() + floorValue) * 100) / 100

    fun snapshot(): CappedAttemptException {
        return CappedAttemptException(
            code = code,
            name = name,
            description = description,
            version = version,
            retries = retries,
            weight = weight,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped attempt exception in status new. */
fun newCappedAttemptException(code: String, name: String): CappedAttemptException {
    return CappedAttemptException(
        code = code,
        name = name,
        description = "",
        version = 0,
        retries = 0,
        weight = 0.0,
        floorValue = 0.0,
        status = CappedAttemptExceptionStatus.NEW,
    )
}
