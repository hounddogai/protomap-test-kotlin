package com.acme.transport.freightpay.audit.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global audit claim is in its life cycle. */
enum class GlobalAuditClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global audit claim. Its code is unique within a tenant. */
data class GlobalAuditClaim(
    var code: String,
    var label: String,
    var notes: String,
    var attempts: Int,
    var channel: String,
    var country: String,
    var visible: Boolean,
    var etaMinutes: Int,
    var startsAt: Int,
    var status: GlobalAuditClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAuditClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAuditClaimStatus): Boolean {
        when (status) {
            GlobalAuditClaimStatus.CREATED -> return next == GlobalAuditClaimStatus.PUBLISHED
            GlobalAuditClaimStatus.VALIDATED -> return next == GlobalAuditClaimStatus.PUBLISHED || next == GlobalAuditClaimStatus.RETIRED
            GlobalAuditClaimStatus.PUBLISHED -> return next == GlobalAuditClaimStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalAuditClaimStatus) {
        if (!canMoveTo(next)) {
            error("global audit claim $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"global audit claim"} $code"

    fun effectiveValue(): Double = round(etaMinutes.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): GlobalAuditClaim {
        return GlobalAuditClaim(
            code = code,
            label = label,
            notes = notes,
            attempts = attempts,
            channel = channel,
            country = country,
            visible = visible,
            etaMinutes = etaMinutes,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global audit claim in status created. */
fun newGlobalAuditClaim(code: String, label: String): GlobalAuditClaim {
    return GlobalAuditClaim(
        code = code,
        label = label,
        notes = "",
        attempts = 0,
        channel = "",
        country = "",
        visible = false,
        etaMinutes = 0,
        startsAt = 0,
        status = GlobalAuditClaimStatus.CREATED,
    )
}
