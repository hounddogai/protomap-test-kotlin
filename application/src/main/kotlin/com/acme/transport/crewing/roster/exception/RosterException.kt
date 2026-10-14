package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The roster exception records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster exception is in its life cycle. */
enum class RosterExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A roster exception as the roster team keeps it. */
data class RosterException(
    var code: String,
    var name: String,
    var capacity: Int,
    var batchSize: Int,
    var retries: Int,
    var graceDays: Int,
    var limit: Int,
    var status: RosterExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterExceptionStatus): Boolean {
        when (status) {
            RosterExceptionStatus.PLANNED -> return next == RosterExceptionStatus.COMPLETED || next == RosterExceptionStatus.CANCELLED
            RosterExceptionStatus.SCHEDULED -> {
                when (next) {
                    RosterExceptionStatus.COMPLETED -> return true
                }
                return false
            }
            RosterExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    RosterExceptionStatus.COMPLETED, RosterExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            RosterExceptionStatus.COMPLETED -> {
                when (next) {
                    RosterExceptionStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterExceptionStatus) {
        if (!canMoveTo(next)) {
            error("roster exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedLoad(): Double = round((batchSize.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): RosterException {
        return RosterException(
            code = code,
            name = name,
            capacity = capacity,
            batchSize = batchSize,
            retries = retries,
            graceDays = graceDays,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a roster exception in status planned. */
fun newRosterException(code: String, name: String): RosterException {
    return RosterException(
        code = code,
        name = name,
        capacity = 0,
        batchSize = 0,
        retries = 0,
        graceDays = 0,
        limit = 0,
        status = RosterExceptionStatus.PLANNED,
    )
}
