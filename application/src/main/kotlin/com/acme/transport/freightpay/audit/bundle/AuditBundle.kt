package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.kit.text.normalize

// The audit bundle records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit bundle is in its life cycle. */
enum class AuditBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A audit bundle. Its code is unique within a tenant. */
data class AuditBundle(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var contactEmail: String,
    var source: String,
    var checksum: String,
    var amount: Double,
    var status: AuditBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditBundleStatus): Boolean {
        when (status) {
            AuditBundleStatus.IDLE -> return next == AuditBundleStatus.STOPPED
            AuditBundleStatus.WARMING -> return next == AuditBundleStatus.READY || next == AuditBundleStatus.STOPPED
            AuditBundleStatus.READY -> {
                when (next) {
                    AuditBundleStatus.DRAINING, AuditBundleStatus.STOPPED -> return true
                }
                return false
            }
            AuditBundleStatus.DRAINING -> {
                when (next) {
                    AuditBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditBundleStatus) {
        if (!canMoveTo(next)) {
            error("audit bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"audit bundle"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = amount

    fun snapshot(): AuditBundle {
        return AuditBundle(
            code = code,
            label = label,
            attributes = attributes,
            contactEmail = contactEmail,
            source = source,
            checksum = checksum,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a audit bundle in status idle. */
fun newAuditBundle(code: String, label: String): AuditBundle {
    return AuditBundle(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        contactEmail = "",
        source = "",
        checksum = "",
        amount = 0.0,
        status = AuditBundleStatus.IDLE,
    )
}
