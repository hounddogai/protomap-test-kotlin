package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending duty period template is in its life cycle. */
enum class PendingDutyPeriodTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending duty period template as the duty period team keeps it. */
data class PendingDutyPeriodTemplate(
    var code: String,
    var label: String,
    var externalRef: String,
    var contactEmail: String,
    var capacity: Int,
    var reviewer: String,
    var depot: String,
    var windowDays: Int,
    var stopCount: Int,
    var category: String,
    var status: PendingDutyPeriodTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDutyPeriodTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDutyPeriodTemplateStatus): Boolean {
        when (status) {
            PendingDutyPeriodTemplateStatus.NEW -> return next == PendingDutyPeriodTemplateStatus.RUNNING
            PendingDutyPeriodTemplateStatus.QUEUED -> return next == PendingDutyPeriodTemplateStatus.RUNNING
            PendingDutyPeriodTemplateStatus.RUNNING -> {
                when (next) {
                    PendingDutyPeriodTemplateStatus.DONE, PendingDutyPeriodTemplateStatus.FAILED -> return true
                }
                return false
            }
            PendingDutyPeriodTemplateStatus.DONE -> return next == PendingDutyPeriodTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingDutyPeriodTemplateStatus) {
        if (!canMoveTo(next)) {
            error("pending duty period template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending duty period template"} $code"

    fun weightedValue(): Double = round((stopCount.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): PendingDutyPeriodTemplate {
        return PendingDutyPeriodTemplate(
            code = code,
            label = label,
            externalRef = externalRef,
            contactEmail = contactEmail,
            capacity = capacity,
            reviewer = reviewer,
            depot = depot,
            windowDays = windowDays,
            stopCount = stopCount,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending duty period template in status new. */
fun newPendingDutyPeriodTemplate(code: String, label: String): PendingDutyPeriodTemplate {
    return PendingDutyPeriodTemplate(
        code = code,
        label = label,
        externalRef = "",
        contactEmail = "",
        capacity = 0,
        reviewer = "",
        depot = "",
        windowDays = 0,
        stopCount = 0,
        category = "",
        status = PendingDutyPeriodTemplateStatus.NEW,
    )
}
