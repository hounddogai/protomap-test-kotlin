package com.acme.transport.freightpay.audit.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit record is in its life cycle. */
enum class AuditRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A audit record as the audit team keeps it. */
data class AuditRecord(
    var code: String,
    var label: String,
    var version: Int,
    var maximum: Double,
    var region: String,
    var description: String,
    var graceDays: Int,
    var depot: String,
    var dock: String,
    var windowDays: Int,
    var margin: Double,
    var status: AuditRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditRecordStatus): Boolean {
        when (status) {
            AuditRecordStatus.CREATED -> {
                when (next) {
                    AuditRecordStatus.PUBLISHED, AuditRecordStatus.RETIRED -> return true
                }
                return false
            }
            AuditRecordStatus.VALIDATED -> {
                when (next) {
                    AuditRecordStatus.PUBLISHED -> return true
                }
                return false
            }
            AuditRecordStatus.PUBLISHED -> {
                when (next) {
                    AuditRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditRecordStatus) {
        if (!canMoveTo(next)) {
            error("audit record $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round((graceDays.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): AuditRecord {
        return AuditRecord(
            code = code,
            label = label,
            version = version,
            maximum = maximum,
            region = region,
            description = description,
            graceDays = graceDays,
            depot = depot,
            dock = dock,
            windowDays = windowDays,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a audit record in status created. */
fun newAuditRecord(code: String, label: String): AuditRecord {
    return AuditRecord(
        code = code,
        label = label,
        version = 0,
        maximum = 0.0,
        region = "",
        description = "",
        graceDays = 0,
        depot = "",
        dock = "",
        windowDays = 0,
        margin = 0.0,
        status = AuditRecordStatus.CREATED,
    )
}
