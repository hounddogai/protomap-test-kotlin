package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.text.normalize

/** Where a primary inspection audit is in its life cycle. */
enum class PrimaryInspectionAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary inspection audit, with what the planners need to act on it. */
data class PrimaryInspectionAudit(
    var code: String,
    var title: String,
    var unit: String,
    var phone: String,
    var lane: String,
    var version: Int,
    var currency: String,
    var contactEmail: String,
    var archived: Boolean,
    var status: PrimaryInspectionAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryInspectionAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryInspectionAuditStatus): Boolean {
        when (status) {
            PrimaryInspectionAuditStatus.NEW -> return next == PrimaryInspectionAuditStatus.QUEUED || next == PrimaryInspectionAuditStatus.RUNNING
            PrimaryInspectionAuditStatus.QUEUED -> return next == PrimaryInspectionAuditStatus.DONE || next == PrimaryInspectionAuditStatus.FAILED
            PrimaryInspectionAuditStatus.RUNNING -> {
                when (next) {
                    PrimaryInspectionAuditStatus.DONE, PrimaryInspectionAuditStatus.FAILED -> return true
                }
                return false
            }
            PrimaryInspectionAuditStatus.DONE -> {
                when (next) {
                    PrimaryInspectionAuditStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryInspectionAuditStatus) {
        if (!canMoveTo(next)) {
            error("primary inspection audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): PrimaryInspectionAudit {
        return PrimaryInspectionAudit(
            code = code,
            title = title,
            unit = unit,
            phone = phone,
            lane = lane,
            version = version,
            currency = currency,
            contactEmail = contactEmail,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary inspection audit in status new. */
fun newPrimaryInspectionAudit(code: String, title: String): PrimaryInspectionAudit {
    return PrimaryInspectionAudit(
        code = code,
        title = title,
        unit = "",
        phone = "",
        lane = "",
        version = 0,
        currency = "",
        contactEmail = "",
        archived = false,
        status = PrimaryInspectionAuditStatus.NEW,
    )
}
