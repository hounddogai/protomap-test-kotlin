package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit batch is in its life cycle. */
enum class AuditBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A audit batch as the audit team keeps it. */
data class AuditBatch(
    var code: String,
    var label: String,
    var source: String,
    var contactEmail: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var ceiling: Double,
    var checksum: String,
    var durationMinutes: Int,
    var status: AuditBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditBatchStatus): Boolean {
        when (status) {
            AuditBatchStatus.OPEN -> {
                when (next) {
                    AuditBatchStatus.IN_REVIEW, AuditBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            AuditBatchStatus.IN_REVIEW -> {
                when (next) {
                    AuditBatchStatus.RESOLVED -> return true
                }
                return false
            }
            AuditBatchStatus.RESOLVED -> return next == AuditBatchStatus.REOPENED || next == AuditBatchStatus.ARCHIVED
            AuditBatchStatus.REOPENED -> {
                when (next) {
                    AuditBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditBatchStatus) {
        if (!canMoveTo(next)) {
            error("audit batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((ceiling - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): AuditBatch {
        return AuditBatch(
            code = code,
            label = label,
            source = source,
            contactEmail = contactEmail,
            name = name,
            attributes = attributes,
            ceiling = ceiling,
            checksum = checksum,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a audit batch in status open. */
fun newAuditBatch(code: String, label: String): AuditBatch {
    return AuditBatch(
        code = code,
        label = label,
        source = "",
        contactEmail = "",
        name = "",
        attributes = mutableMapOf<String, String>(),
        ceiling = 0.0,
        checksum = "",
        durationMinutes = 0,
        status = AuditBatchStatus.OPEN,
    )
}
