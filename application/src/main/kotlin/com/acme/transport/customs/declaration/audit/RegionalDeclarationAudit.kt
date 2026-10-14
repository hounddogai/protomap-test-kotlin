package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional declaration audit is in its life cycle. */
enum class RegionalDeclarationAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One regional declaration audit, with what the planners need to act on it. */
data class RegionalDeclarationAudit(
    var code: String,
    var label: String,
    var checksum: String,
    var routeCode: String,
    var expiresAt: Int,
    var minimum: Double,
    var archived: Boolean,
    var title: String,
    var contactEmail: String,
    var score: Double,
    var status: RegionalDeclarationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDeclarationAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDeclarationAuditStatus): Boolean {
        when (status) {
            RegionalDeclarationAuditStatus.PLANNED -> return next == RegionalDeclarationAuditStatus.SCHEDULED
            RegionalDeclarationAuditStatus.SCHEDULED -> return next == RegionalDeclarationAuditStatus.IN_PROGRESS || next == RegionalDeclarationAuditStatus.COMPLETED
            RegionalDeclarationAuditStatus.IN_PROGRESS -> return next == RegionalDeclarationAuditStatus.COMPLETED || next == RegionalDeclarationAuditStatus.CANCELLED
            RegionalDeclarationAuditStatus.COMPLETED -> return next == RegionalDeclarationAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RegionalDeclarationAuditStatus) {
        if (!canMoveTo(next)) {
            error("regional declaration audit $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round((minimum - score) * 100) / 100

    fun snapshot(): RegionalDeclarationAudit {
        return RegionalDeclarationAudit(
            code = code,
            label = label,
            checksum = checksum,
            routeCode = routeCode,
            expiresAt = expiresAt,
            minimum = minimum,
            archived = archived,
            title = title,
            contactEmail = contactEmail,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional declaration audit in status planned. */
fun newRegionalDeclarationAudit(code: String, label: String): RegionalDeclarationAudit {
    return RegionalDeclarationAudit(
        code = code,
        label = label,
        checksum = "",
        routeCode = "",
        expiresAt = 0,
        minimum = 0.0,
        archived = false,
        title = "",
        contactEmail = "",
        score = 0.0,
        status = RegionalDeclarationAuditStatus.PLANNED,
    )
}
