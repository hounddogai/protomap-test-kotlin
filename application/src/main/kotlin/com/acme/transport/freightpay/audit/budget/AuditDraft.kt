package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit draft is in its life cycle. */
enum class AuditDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a audit draft and its current status. */
data class AuditDraft(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var attempts: Int,
    var ceiling: Double,
    var updatedAt: Int,
    var retries: Int,
    var quantity: Int,
    var floorValue: Double,
    var status: AuditDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditDraftStatus): Boolean {
        when (status) {
            AuditDraftStatus.DRAFT -> {
                when (next) {
                    AuditDraftStatus.ACTIVE, AuditDraftStatus.SUSPENDED -> return true
                }
                return false
            }
            AuditDraftStatus.PENDING -> return next == AuditDraftStatus.SUSPENDED || next == AuditDraftStatus.CLOSED
            AuditDraftStatus.ACTIVE -> {
                when (next) {
                    AuditDraftStatus.SUSPENDED, AuditDraftStatus.CLOSED -> return true
                }
                return false
            }
            AuditDraftStatus.SUSPENDED -> return next == AuditDraftStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AuditDraftStatus) {
        if (!canMoveTo(next)) {
            error("audit draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"audit draft"} $code"

    fun weightedExposure(): Double = round((ceiling + retries.toDouble()) * 100) / 100

    fun snapshot(): AuditDraft {
        return AuditDraft(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            attempts = attempts,
            ceiling = ceiling,
            updatedAt = updatedAt,
            retries = retries,
            quantity = quantity,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a audit draft in status draft. */
fun newAuditDraft(code: String, name: String): AuditDraft {
    return AuditDraft(
        code = code,
        name = name,
        durationMinutes = 0,
        attempts = 0,
        ceiling = 0.0,
        updatedAt = 0,
        retries = 0,
        quantity = 0,
        floorValue = 0.0,
        status = AuditDraftStatus.DRAFT,
    )
}
