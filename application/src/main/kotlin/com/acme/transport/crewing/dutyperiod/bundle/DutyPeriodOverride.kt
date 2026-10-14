package com.acme.transport.crewing.dutyperiod.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period override is in its life cycle. */
enum class DutyPeriodOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A duty period override. Its code is unique within a tenant. */
data class DutyPeriodOverride(
    var code: String,
    var label: String,
    var batchSize: Int,
    var visible: Boolean,
    var currency: String,
    var threshold: Double,
    var priority: Int,
    var archived: Boolean,
    var status: DutyPeriodOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodOverrideStatus): Boolean {
        when (status) {
            DutyPeriodOverrideStatus.OPEN -> return next == DutyPeriodOverrideStatus.REOPENED || next == DutyPeriodOverrideStatus.ARCHIVED
            DutyPeriodOverrideStatus.IN_REVIEW -> return next == DutyPeriodOverrideStatus.RESOLVED
            DutyPeriodOverrideStatus.RESOLVED -> return next == DutyPeriodOverrideStatus.REOPENED
            DutyPeriodOverrideStatus.REOPENED -> {
                when (next) {
                    DutyPeriodOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodOverrideStatus) {
        if (!canMoveTo(next)) {
            error("duty period override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedLoad(): Double = round((priority.toDouble() - threshold) * 100) / 100

    fun snapshot(): DutyPeriodOverride {
        return DutyPeriodOverride(
            code = code,
            label = label,
            batchSize = batchSize,
            visible = visible,
            currency = currency,
            threshold = threshold,
            priority = priority,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a duty period override in status open. */
fun newDutyPeriodOverride(code: String, label: String): DutyPeriodOverride {
    return DutyPeriodOverride(
        code = code,
        label = label,
        batchSize = 0,
        visible = false,
        currency = "",
        threshold = 0.0,
        priority = 0,
        archived = false,
        status = DutyPeriodOverrideStatus.OPEN,
    )
}
