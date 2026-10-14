package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient audit record is in its life cycle. */
enum class LenientAuditRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One lenient audit record, with what the planners need to act on it. */
data class LenientAuditRecord(
    var code: String,
    var title: String,
    var version: Int,
    var attributes: MutableMap<String, String>,
    var amount: Double,
    var sequence: Int,
    var discount: Double,
    var currency: String,
    var ceiling: Double,
    var contactEmail: String,
    var reviewer: String,
    var status: LenientAuditRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAuditRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAuditRecordStatus): Boolean {
        when (status) {
            LenientAuditRecordStatus.PROPOSED -> return next == LenientAuditRecordStatus.WITHDRAWN
            LenientAuditRecordStatus.APPROVED -> {
                when (next) {
                    LenientAuditRecordStatus.REJECTED, LenientAuditRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            LenientAuditRecordStatus.REJECTED -> {
                when (next) {
                    LenientAuditRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientAuditRecordStatus) {
        if (!canMoveTo(next)) {
            error("lenient audit record $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round((discount + version.toDouble()) * 100) / 100

    fun snapshot(): LenientAuditRecord {
        return LenientAuditRecord(
            code = code,
            title = title,
            version = version,
            attributes = attributes,
            amount = amount,
            sequence = sequence,
            discount = discount,
            currency = currency,
            ceiling = ceiling,
            contactEmail = contactEmail,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient audit record in status proposed. */
fun newLenientAuditRecord(code: String, title: String): LenientAuditRecord {
    return LenientAuditRecord(
        code = code,
        title = title,
        version = 0,
        attributes = mutableMapOf<String, String>(),
        amount = 0.0,
        sequence = 0,
        discount = 0.0,
        currency = "",
        ceiling = 0.0,
        contactEmail = "",
        reviewer = "",
        status = LenientAuditRecordStatus.PROPOSED,
    )
}
