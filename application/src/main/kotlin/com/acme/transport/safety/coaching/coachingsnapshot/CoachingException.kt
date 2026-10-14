package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching exception is in its life cycle. */
enum class CoachingExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a coaching exception and its current status. */
data class CoachingException(
    var code: String,
    var label: String,
    var checksum: String,
    var region: String,
    var timezone: String,
    var weight: Double,
    var segment: String,
    var quantity: Int,
    var pallets: Int,
    var status: CoachingExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingExceptionStatus): Boolean {
        when (status) {
            CoachingExceptionStatus.OPEN -> return next == CoachingExceptionStatus.IN_REVIEW || next == CoachingExceptionStatus.REOPENED
            CoachingExceptionStatus.IN_REVIEW -> {
                when (next) {
                    CoachingExceptionStatus.RESOLVED, CoachingExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            CoachingExceptionStatus.RESOLVED -> return next == CoachingExceptionStatus.ARCHIVED
            CoachingExceptionStatus.REOPENED -> return next == CoachingExceptionStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CoachingExceptionStatus) {
        if (!canMoveTo(next)) {
            error("coaching exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"coaching exception"} $code"

    fun effectiveLoad(): Double = round((weight - quantity.toDouble()) * 100) / 100

    fun snapshot(): CoachingException {
        return CoachingException(
            code = code,
            label = label,
            checksum = checksum,
            region = region,
            timezone = timezone,
            weight = weight,
            segment = segment,
            quantity = quantity,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a coaching exception in status open. */
fun newCoachingException(code: String, label: String): CoachingException {
    return CoachingException(
        code = code,
        label = label,
        checksum = "",
        region = "",
        timezone = "",
        weight = 0.0,
        segment = "",
        quantity = 0,
        pallets = 0,
        status = CoachingExceptionStatus.OPEN,
    )
}
