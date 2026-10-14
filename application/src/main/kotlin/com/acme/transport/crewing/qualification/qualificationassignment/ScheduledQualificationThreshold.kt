package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.text.normalize

/** Where a scheduled qualification threshold is in its life cycle. */
enum class ScheduledQualificationThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a scheduled qualification threshold and its current status. */
data class ScheduledQualificationThreshold(
    var code: String,
    var name: String,
    var checksum: String,
    var version: Int,
    var pinned: Boolean,
    var status: ScheduledQualificationThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledQualificationThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledQualificationThresholdStatus): Boolean {
        when (status) {
            ScheduledQualificationThresholdStatus.OPEN -> return next == ScheduledQualificationThresholdStatus.RESOLVED || next == ScheduledQualificationThresholdStatus.ARCHIVED
            ScheduledQualificationThresholdStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledQualificationThresholdStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledQualificationThresholdStatus.RESOLVED -> return next == ScheduledQualificationThresholdStatus.ARCHIVED
            ScheduledQualificationThresholdStatus.REOPENED -> return next == ScheduledQualificationThresholdStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledQualificationThresholdStatus) {
        if (!canMoveTo(next)) {
            error("scheduled qualification threshold $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): ScheduledQualificationThreshold {
        return ScheduledQualificationThreshold(
            code = code,
            name = name,
            checksum = checksum,
            version = version,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled qualification threshold in status open. */
fun newScheduledQualificationThreshold(code: String, name: String): ScheduledQualificationThreshold {
    return ScheduledQualificationThreshold(
        code = code,
        name = name,
        checksum = "",
        version = 0,
        pinned = false,
        status = ScheduledQualificationThresholdStatus.OPEN,
    )
}
