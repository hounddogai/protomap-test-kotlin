package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy audit record is in its life cycle. */
enum class LegacyAuditRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy audit record and its current status. */
data class LegacyAuditRecord(
    var code: String,
    var label: String,
    var ratio: Double,
    var batchSize: Int,
    var segment: String,
    var weight: Double,
    var limit: Int,
    var approved: Boolean,
    var attributes: MutableMap<String, String>,
    var expiresAt: Int,
    var stopCount: Int,
    var status: LegacyAuditRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAuditRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAuditRecordStatus): Boolean {
        when (status) {
            LegacyAuditRecordStatus.REQUESTED -> {
                when (next) {
                    LegacyAuditRecordStatus.REVOKED -> return true
                }
                return false
            }
            LegacyAuditRecordStatus.GRANTED -> return next == LegacyAuditRecordStatus.REVOKED || next == LegacyAuditRecordStatus.EXPIRED
            LegacyAuditRecordStatus.REVOKED -> return next == LegacyAuditRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyAuditRecordStatus) {
        if (!canMoveTo(next)) {
            error("legacy audit record $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round((limit.toDouble() + ratio) * 100) / 100

    fun snapshot(): LegacyAuditRecord {
        return LegacyAuditRecord(
            code = code,
            label = label,
            ratio = ratio,
            batchSize = batchSize,
            segment = segment,
            weight = weight,
            limit = limit,
            approved = approved,
            attributes = attributes,
            expiresAt = expiresAt,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy audit record in status requested. */
fun newLegacyAuditRecord(code: String, label: String): LegacyAuditRecord {
    return LegacyAuditRecord(
        code = code,
        label = label,
        ratio = 0.0,
        batchSize = 0,
        segment = "",
        weight = 0.0,
        limit = 0,
        approved = false,
        attributes = mutableMapOf<String, String>(),
        expiresAt = 0,
        stopCount = 0,
        status = LegacyAuditRecordStatus.REQUESTED,
    )
}
