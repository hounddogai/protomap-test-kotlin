package com.acme.transport.emissions.idling.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling exception is in its life cycle. */
enum class IdlingExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A idling exception. Its code is unique within a tenant. */
data class IdlingException(
    var code: String,
    var label: String,
    var capacity: Int,
    var category: String,
    var rate: Double,
    var vehicleId: String,
    var durationMinutes: Int,
    var score: Double,
    var archived: Boolean,
    var status: IdlingExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingExceptionStatus): Boolean {
        when (status) {
            IdlingExceptionStatus.DRAFT -> return next == IdlingExceptionStatus.PENDING || next == IdlingExceptionStatus.SUSPENDED
            IdlingExceptionStatus.PENDING -> {
                when (next) {
                    IdlingExceptionStatus.ACTIVE, IdlingExceptionStatus.CLOSED -> return true
                }
                return false
            }
            IdlingExceptionStatus.ACTIVE -> return next == IdlingExceptionStatus.SUSPENDED
            IdlingExceptionStatus.SUSPENDED -> {
                when (next) {
                    IdlingExceptionStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingExceptionStatus) {
        if (!canMoveTo(next)) {
            error("idling exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveVolume(): Double = round((capacity.toDouble() - score) * 100) / 100

    fun snapshot(): IdlingException {
        return IdlingException(
            code = code,
            label = label,
            capacity = capacity,
            category = category,
            rate = rate,
            vehicleId = vehicleId,
            durationMinutes = durationMinutes,
            score = score,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a idling exception in status draft. */
fun newIdlingException(code: String, label: String): IdlingException {
    return IdlingException(
        code = code,
        label = label,
        capacity = 0,
        category = "",
        rate = 0.0,
        vehicleId = "",
        durationMinutes = 0,
        score = 0.0,
        archived = false,
        status = IdlingExceptionStatus.DRAFT,
    )
}
