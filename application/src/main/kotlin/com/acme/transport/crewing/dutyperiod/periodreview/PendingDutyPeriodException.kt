package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending duty period exception is in its life cycle. */
enum class PendingDutyPeriodExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a pending duty period exception and its current status. */
data class PendingDutyPeriodException(
    var code: String,
    var label: String,
    var shiftCode: String,
    var maximum: Double,
    var limit: Int,
    var weight: Double,
    var tags: MutableList<String>,
    var category: String,
    var status: PendingDutyPeriodExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDutyPeriodExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDutyPeriodExceptionStatus): Boolean {
        when (status) {
            PendingDutyPeriodExceptionStatus.OPEN -> {
                when (next) {
                    PendingDutyPeriodExceptionStatus.RESOLVED -> return true
                }
                return false
            }
            PendingDutyPeriodExceptionStatus.IN_REVIEW -> return next == PendingDutyPeriodExceptionStatus.RESOLVED || next == PendingDutyPeriodExceptionStatus.REOPENED
            PendingDutyPeriodExceptionStatus.RESOLVED -> return next == PendingDutyPeriodExceptionStatus.REOPENED || next == PendingDutyPeriodExceptionStatus.ARCHIVED
            PendingDutyPeriodExceptionStatus.REOPENED -> {
                when (next) {
                    PendingDutyPeriodExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDutyPeriodExceptionStatus) {
        if (!canMoveTo(next)) {
            error("pending duty period exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round((weight + maximum) * 100) / 100

    fun snapshot(): PendingDutyPeriodException {
        return PendingDutyPeriodException(
            code = code,
            label = label,
            shiftCode = shiftCode,
            maximum = maximum,
            limit = limit,
            weight = weight,
            tags = tags,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending duty period exception in status open. */
fun newPendingDutyPeriodException(code: String, label: String): PendingDutyPeriodException {
    return PendingDutyPeriodException(
        code = code,
        label = label,
        shiftCode = "",
        maximum = 0.0,
        limit = 0,
        weight = 0.0,
        tags = mutableListOf<String>(),
        category = "",
        status = PendingDutyPeriodExceptionStatus.OPEN,
    )
}
