package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary audit override records of freightpay's audit work, and the rules, stores, and calculations
// around them.

/** Where a primary audit override is in its life cycle. */
enum class PrimaryAuditOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary audit override as the audit team keeps it. */
data class PrimaryAuditOverride(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var durationMinutes: Int,
    var attempts: Int,
    var category: String,
    var status: PrimaryAuditOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAuditOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAuditOverrideStatus): Boolean {
        when (status) {
            PrimaryAuditOverrideStatus.CREATED -> return next == PrimaryAuditOverrideStatus.PUBLISHED
            PrimaryAuditOverrideStatus.VALIDATED -> return next == PrimaryAuditOverrideStatus.PUBLISHED
            PrimaryAuditOverrideStatus.PUBLISHED -> return next == PrimaryAuditOverrideStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryAuditOverrideStatus) {
        if (!canMoveTo(next)) {
            error("primary audit override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary audit override"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round((durationMinutes.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): PrimaryAuditOverride {
        return PrimaryAuditOverride(
            code = code,
            label = label,
            attributes = attributes,
            durationMinutes = durationMinutes,
            attempts = attempts,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary audit override in status created. */
fun newPrimaryAuditOverride(code: String, label: String): PrimaryAuditOverride {
    return PrimaryAuditOverride(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        durationMinutes = 0,
        attempts = 0,
        category = "",
        status = PrimaryAuditOverrideStatus.CREATED,
    )
}
