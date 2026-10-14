package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.text.normalize

/** Where a audit manifest is in its life cycle. */
enum class AuditManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One audit manifest, with what the planners need to act on it. */
data class AuditManifest(
    var code: String,
    var title: String,
    var contactEmail: String,
    var notes: String,
    var ownerId: String,
    var amount: Double,
    var status: AuditManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditManifestStatus): Boolean {
        when (status) {
            AuditManifestStatus.CREATED -> {
                when (next) {
                    AuditManifestStatus.VALIDATED, AuditManifestStatus.RETIRED -> return true
                }
                return false
            }
            AuditManifestStatus.VALIDATED -> {
                when (next) {
                    AuditManifestStatus.PUBLISHED -> return true
                }
                return false
            }
            AuditManifestStatus.PUBLISHED -> return next == AuditManifestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AuditManifestStatus) {
        if (!canMoveTo(next)) {
            error("audit manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = amount

    fun snapshot(): AuditManifest {
        return AuditManifest(
            code = code,
            title = title,
            contactEmail = contactEmail,
            notes = notes,
            ownerId = ownerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a audit manifest in status created. */
fun newAuditManifest(code: String, title: String): AuditManifest {
    return AuditManifest(
        code = code,
        title = title,
        contactEmail = "",
        notes = "",
        ownerId = "",
        amount = 0.0,
        status = AuditManifestStatus.CREATED,
    )
}
