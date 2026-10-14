package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.text.normalize

/** Where a legacy audit manifest is in its life cycle. */
enum class LegacyAuditManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One legacy audit manifest, with what the planners need to act on it. */
data class LegacyAuditManifest(
    var code: String,
    var label: String,
    var maximum: Double,
    var approved: Boolean,
    var description: String,
    var status: LegacyAuditManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAuditManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAuditManifestStatus): Boolean {
        when (status) {
            LegacyAuditManifestStatus.PROPOSED -> return next == LegacyAuditManifestStatus.REJECTED || next == LegacyAuditManifestStatus.WITHDRAWN
            LegacyAuditManifestStatus.APPROVED -> {
                when (next) {
                    LegacyAuditManifestStatus.REJECTED -> return true
                }
                return false
            }
            LegacyAuditManifestStatus.REJECTED -> return next == LegacyAuditManifestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyAuditManifestStatus) {
        if (!canMoveTo(next)) {
            error("legacy audit manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy audit manifest"} $code"

    fun measuredValue(): Double = maximum

    fun snapshot(): LegacyAuditManifest {
        return LegacyAuditManifest(
            code = code,
            label = label,
            maximum = maximum,
            approved = approved,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy audit manifest in status proposed. */
fun newLegacyAuditManifest(code: String, label: String): LegacyAuditManifest {
    return LegacyAuditManifest(
        code = code,
        label = label,
        maximum = 0.0,
        approved = false,
        description = "",
        status = LegacyAuditManifestStatus.PROPOSED,
    )
}
