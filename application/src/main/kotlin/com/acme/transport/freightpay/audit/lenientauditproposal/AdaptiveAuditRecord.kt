package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive audit record is in its life cycle. */
enum class AdaptiveAuditRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One adaptive audit record, with what the planners need to act on it. */
data class AdaptiveAuditRecord(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var etaMinutes: Int,
    var version: Int,
    var status: AdaptiveAuditRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAuditRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAuditRecordStatus): Boolean {
        when (status) {
            AdaptiveAuditRecordStatus.CREATED -> return next == AdaptiveAuditRecordStatus.PUBLISHED || next == AdaptiveAuditRecordStatus.RETIRED
            AdaptiveAuditRecordStatus.VALIDATED -> return next == AdaptiveAuditRecordStatus.RETIRED
            AdaptiveAuditRecordStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveAuditRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAuditRecordStatus) {
        if (!canMoveTo(next)) {
            error("adaptive audit record $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round((version.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveAuditRecord {
        return AdaptiveAuditRecord(
            code = code,
            label = label,
            pinned = pinned,
            etaMinutes = etaMinutes,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive audit record in status created. */
fun newAdaptiveAuditRecord(code: String, label: String): AdaptiveAuditRecord {
    return AdaptiveAuditRecord(
        code = code,
        label = label,
        pinned = false,
        etaMinutes = 0,
        version = 0,
        status = AdaptiveAuditRecordStatus.CREATED,
    )
}
