package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending audit record records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a pending audit record is in its life cycle. */
enum class PendingAuditRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending audit record. Its code is unique within a tenant. */
data class PendingAuditRecord(
    var code: String,
    var name: String,
    var quantity: Int,
    var budget: Double,
    var checksum: String,
    var priority: Int,
    var amount: Double,
    var reviewer: String,
    var rate: Double,
    var currency: String,
    var status: PendingAuditRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAuditRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAuditRecordStatus): Boolean {
        when (status) {
            PendingAuditRecordStatus.REQUESTED -> {
                when (next) {
                    PendingAuditRecordStatus.GRANTED, PendingAuditRecordStatus.REVOKED -> return true
                }
                return false
            }
            PendingAuditRecordStatus.GRANTED -> return next == PendingAuditRecordStatus.REVOKED || next == PendingAuditRecordStatus.EXPIRED
            PendingAuditRecordStatus.REVOKED -> return next == PendingAuditRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingAuditRecordStatus) {
        if (!canMoveTo(next)) {
            error("pending audit record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending audit record"} $code"

    fun effectiveTotal(): Double = round((amount + rate) * 100) / 100

    fun snapshot(): PendingAuditRecord {
        return PendingAuditRecord(
            code = code,
            name = name,
            quantity = quantity,
            budget = budget,
            checksum = checksum,
            priority = priority,
            amount = amount,
            reviewer = reviewer,
            rate = rate,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending audit record in status requested. */
fun newPendingAuditRecord(code: String, name: String): PendingAuditRecord {
    return PendingAuditRecord(
        code = code,
        name = name,
        quantity = 0,
        budget = 0.0,
        checksum = "",
        priority = 0,
        amount = 0.0,
        reviewer = "",
        rate = 0.0,
        currency = "",
        status = PendingAuditRecordStatus.REQUESTED,
    )
}
