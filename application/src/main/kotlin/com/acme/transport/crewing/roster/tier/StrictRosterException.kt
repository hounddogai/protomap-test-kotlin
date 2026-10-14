package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict roster exception is in its life cycle. */
enum class StrictRosterExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict roster exception. Its code is unique within a tenant. */
data class StrictRosterException(
    var code: String,
    var label: String,
    var cost: Double,
    var fuelLiters: Double,
    var retries: Int,
    var score: Double,
    var budget: Double,
    var dock: String,
    var contactEmail: String,
    var status: StrictRosterExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRosterExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRosterExceptionStatus): Boolean {
        when (status) {
            StrictRosterExceptionStatus.PLANNED -> return next == StrictRosterExceptionStatus.IN_PROGRESS || next == StrictRosterExceptionStatus.COMPLETED
            StrictRosterExceptionStatus.SCHEDULED -> {
                when (next) {
                    StrictRosterExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            StrictRosterExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    StrictRosterExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            StrictRosterExceptionStatus.COMPLETED -> {
                when (next) {
                    StrictRosterExceptionStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRosterExceptionStatus) {
        if (!canMoveTo(next)) {
            error("strict roster exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict roster exception"} $code"

    fun projectedLoad(): Double = round((fuelLiters + score) * 100) / 100

    fun snapshot(): StrictRosterException {
        return StrictRosterException(
            code = code,
            label = label,
            cost = cost,
            fuelLiters = fuelLiters,
            retries = retries,
            score = score,
            budget = budget,
            dock = dock,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict roster exception in status planned. */
fun newStrictRosterException(code: String, label: String): StrictRosterException {
    return StrictRosterException(
        code = code,
        label = label,
        cost = 0.0,
        fuelLiters = 0.0,
        retries = 0,
        score = 0.0,
        budget = 0.0,
        dock = "",
        contactEmail = "",
        status = StrictRosterExceptionStatus.PLANNED,
    )
}
