package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit claim is in its life cycle. */
enum class AuditClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A audit claim as the audit team keeps it. */
data class AuditClaim(
    var code: String,
    var label: String,
    var contactEmail: String,
    var floorValue: Double,
    var visible: Boolean,
    var active: Boolean,
    var version: Int,
    var status: AuditClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditClaimStatus): Boolean {
        when (status) {
            AuditClaimStatus.DRAFT -> {
                when (next) {
                    AuditClaimStatus.CLOSED -> return true
                }
                return false
            }
            AuditClaimStatus.PENDING -> {
                when (next) {
                    AuditClaimStatus.ACTIVE -> return true
                }
                return false
            }
            AuditClaimStatus.ACTIVE -> return next == AuditClaimStatus.SUSPENDED || next == AuditClaimStatus.CLOSED
            AuditClaimStatus.SUSPENDED -> {
                when (next) {
                    AuditClaimStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditClaimStatus) {
        if (!canMoveTo(next)) {
            error("audit claim $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round((version.toDouble() - floorValue) * 100) / 100

    fun snapshot(): AuditClaim {
        return AuditClaim(
            code = code,
            label = label,
            contactEmail = contactEmail,
            floorValue = floorValue,
            visible = visible,
            active = active,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a audit claim in status draft. */
fun newAuditClaim(code: String, label: String): AuditClaim {
    return AuditClaim(
        code = code,
        label = label,
        contactEmail = "",
        floorValue = 0.0,
        visible = false,
        active = false,
        version = 0,
        status = AuditClaimStatus.DRAFT,
    )
}
