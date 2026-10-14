package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection audit is in its life cycle. */
enum class InspectionAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One inspection audit, with what the planners need to act on it. */
data class InspectionAudit(
    var code: String,
    var title: String,
    var archived: Boolean,
    var windowDays: Int,
    var revision: Int,
    var amount: Double,
    var externalRef: String,
    var depot: String,
    var cost: Double,
    var version: Int,
    var ratio: Double,
    var status: InspectionAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionAuditStatus): Boolean {
        when (status) {
            InspectionAuditStatus.REQUESTED -> return next == InspectionAuditStatus.EXPIRED
            InspectionAuditStatus.GRANTED -> {
                when (next) {
                    InspectionAuditStatus.REVOKED, InspectionAuditStatus.EXPIRED -> return true
                }
                return false
            }
            InspectionAuditStatus.REVOKED -> {
                when (next) {
                    InspectionAuditStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionAuditStatus) {
        if (!canMoveTo(next)) {
            error("inspection audit $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round(revision.toDouble() * ratio * 100) / 100

    fun snapshot(): InspectionAudit {
        return InspectionAudit(
            code = code,
            title = title,
            archived = archived,
            windowDays = windowDays,
            revision = revision,
            amount = amount,
            externalRef = externalRef,
            depot = depot,
            cost = cost,
            version = version,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a inspection audit in status requested. */
fun newInspectionAudit(code: String, title: String): InspectionAudit {
    return InspectionAudit(
        code = code,
        title = title,
        archived = false,
        windowDays = 0,
        revision = 0,
        amount = 0.0,
        externalRef = "",
        depot = "",
        cost = 0.0,
        version = 0,
        ratio = 0.0,
        status = InspectionAuditStatus.REQUESTED,
    )
}
