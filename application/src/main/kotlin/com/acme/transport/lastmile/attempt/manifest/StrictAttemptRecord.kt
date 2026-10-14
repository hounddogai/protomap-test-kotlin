package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict attempt record is in its life cycle. */
enum class StrictAttemptRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A strict attempt record. Its code is unique within a tenant. */
data class StrictAttemptRecord(
    var code: String,
    var name: String,
    var maximum: Double,
    var currency: String,
    var distanceKm: Double,
    var summary: String,
    var weight: Double,
    var minimum: Double,
    var checksum: String,
    var unit: String,
    var status: StrictAttemptRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAttemptRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAttemptRecordStatus): Boolean {
        when (status) {
            StrictAttemptRecordStatus.PROPOSED -> return next == StrictAttemptRecordStatus.WITHDRAWN
            StrictAttemptRecordStatus.APPROVED -> return next == StrictAttemptRecordStatus.REJECTED || next == StrictAttemptRecordStatus.WITHDRAWN
            StrictAttemptRecordStatus.REJECTED -> {
                when (next) {
                    StrictAttemptRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictAttemptRecordStatus) {
        if (!canMoveTo(next)) {
            error("strict attempt record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedLoad(): Double = round((distanceKm - weight) * 100) / 100

    fun snapshot(): StrictAttemptRecord {
        return StrictAttemptRecord(
            code = code,
            name = name,
            maximum = maximum,
            currency = currency,
            distanceKm = distanceKm,
            summary = summary,
            weight = weight,
            minimum = minimum,
            checksum = checksum,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict attempt record in status proposed. */
fun newStrictAttemptRecord(code: String, name: String): StrictAttemptRecord {
    return StrictAttemptRecord(
        code = code,
        name = name,
        maximum = 0.0,
        currency = "",
        distanceKm = 0.0,
        summary = "",
        weight = 0.0,
        minimum = 0.0,
        checksum = "",
        unit = "",
        status = StrictAttemptRecordStatus.PROPOSED,
    )
}
