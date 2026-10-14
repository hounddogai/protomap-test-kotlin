package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.text.normalize

/** Where a legacy hold exception is in its life cycle. */
enum class LegacyHoldExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One legacy hold exception, with what the planners need to act on it. */
data class LegacyHoldException(
    var code: String,
    var name: String,
    var depot: String,
    var notes: String,
    var source: String,
    var checksum: String,
    var category: String,
    var distanceKm: Double,
    var status: LegacyHoldExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyHoldExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyHoldExceptionStatus): Boolean {
        when (status) {
            LegacyHoldExceptionStatus.NEW -> return next == LegacyHoldExceptionStatus.RUNNING || next == LegacyHoldExceptionStatus.FAILED
            LegacyHoldExceptionStatus.QUEUED -> return next == LegacyHoldExceptionStatus.RUNNING || next == LegacyHoldExceptionStatus.DONE
            LegacyHoldExceptionStatus.RUNNING -> return next == LegacyHoldExceptionStatus.DONE || next == LegacyHoldExceptionStatus.FAILED
            LegacyHoldExceptionStatus.DONE -> return next == LegacyHoldExceptionStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyHoldExceptionStatus) {
        if (!canMoveTo(next)) {
            error("legacy hold exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): LegacyHoldException {
        return LegacyHoldException(
            code = code,
            name = name,
            depot = depot,
            notes = notes,
            source = source,
            checksum = checksum,
            category = category,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy hold exception in status new. */
fun newLegacyHoldException(code: String, name: String): LegacyHoldException {
    return LegacyHoldException(
        code = code,
        name = name,
        depot = "",
        notes = "",
        source = "",
        checksum = "",
        category = "",
        distanceKm = 0.0,
        status = LegacyHoldExceptionStatus.NEW,
    )
}
