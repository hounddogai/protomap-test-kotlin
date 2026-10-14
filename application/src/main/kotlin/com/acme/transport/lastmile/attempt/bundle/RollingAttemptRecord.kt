package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.text.normalize

/** Where a rolling attempt record is in its life cycle. */
enum class RollingAttemptRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling attempt record as the attempt team keeps it. */
data class RollingAttemptRecord(
    var code: String,
    var label: String,
    var depot: String,
    var threshold: Double,
    var notes: String,
    var driverId: String,
    var region: String,
    var shiftCode: String,
    var active: Boolean,
    var phone: String,
    var segment: String,
    var status: RollingAttemptRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAttemptRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAttemptRecordStatus): Boolean {
        when (status) {
            RollingAttemptRecordStatus.CREATED -> {
                when (next) {
                    RollingAttemptRecordStatus.VALIDATED, RollingAttemptRecordStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingAttemptRecordStatus.VALIDATED -> {
                when (next) {
                    RollingAttemptRecordStatus.RETIRED -> return true
                }
                return false
            }
            RollingAttemptRecordStatus.PUBLISHED -> return next == RollingAttemptRecordStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingAttemptRecordStatus) {
        if (!canMoveTo(next)) {
            error("rolling attempt record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling attempt record"} $code"

    fun measuredValue(): Double = threshold

    fun snapshot(): RollingAttemptRecord {
        return RollingAttemptRecord(
            code = code,
            label = label,
            depot = depot,
            threshold = threshold,
            notes = notes,
            driverId = driverId,
            region = region,
            shiftCode = shiftCode,
            active = active,
            phone = phone,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling attempt record in status created. */
fun newRollingAttemptRecord(code: String, label: String): RollingAttemptRecord {
    return RollingAttemptRecord(
        code = code,
        label = label,
        depot = "",
        threshold = 0.0,
        notes = "",
        driverId = "",
        region = "",
        shiftCode = "",
        active = false,
        phone = "",
        segment = "",
        status = RollingAttemptRecordStatus.CREATED,
    )
}
