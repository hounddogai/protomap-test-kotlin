package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.text.normalize

/** Where a scheduled inspection agreement is in its life cycle. */
enum class ScheduledInspectionAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled inspection agreement as the inspection team keeps it. */
data class ScheduledInspectionAgreement(
    var code: String,
    var name: String,
    var visible: Boolean,
    var windowDays: Int,
    var pinned: Boolean,
    var status: ScheduledInspectionAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledInspectionAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledInspectionAgreementStatus): Boolean {
        when (status) {
            ScheduledInspectionAgreementStatus.NEW -> return next == ScheduledInspectionAgreementStatus.DONE || next == ScheduledInspectionAgreementStatus.FAILED
            ScheduledInspectionAgreementStatus.QUEUED -> {
                when (next) {
                    ScheduledInspectionAgreementStatus.RUNNING, ScheduledInspectionAgreementStatus.DONE -> return true
                }
                return false
            }
            ScheduledInspectionAgreementStatus.RUNNING -> return next == ScheduledInspectionAgreementStatus.FAILED
            ScheduledInspectionAgreementStatus.DONE -> {
                when (next) {
                    ScheduledInspectionAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledInspectionAgreementStatus) {
        if (!canMoveTo(next)) {
            error("scheduled inspection agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): ScheduledInspectionAgreement {
        return ScheduledInspectionAgreement(
            code = code,
            name = name,
            visible = visible,
            windowDays = windowDays,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled inspection agreement in status new. */
fun newScheduledInspectionAgreement(code: String, name: String): ScheduledInspectionAgreement {
    return ScheduledInspectionAgreement(
        code = code,
        name = name,
        visible = false,
        windowDays = 0,
        pinned = false,
        status = ScheduledInspectionAgreementStatus.NEW,
    )
}
