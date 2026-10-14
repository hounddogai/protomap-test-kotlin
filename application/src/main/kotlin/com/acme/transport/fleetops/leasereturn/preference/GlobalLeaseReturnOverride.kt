package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.text.normalize

/** Where a global lease return override is in its life cycle. */
enum class GlobalLeaseReturnOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global lease return override. Its code is unique within a tenant. */
data class GlobalLeaseReturnOverride(
    var code: String,
    var label: String,
    var phone: String,
    var summary: String,
    var graceDays: Int,
    var attributes: MutableMap<String, String>,
    var status: GlobalLeaseReturnOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalLeaseReturnOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalLeaseReturnOverrideStatus): Boolean {
        when (status) {
            GlobalLeaseReturnOverrideStatus.REQUESTED -> return next == GlobalLeaseReturnOverrideStatus.REVOKED || next == GlobalLeaseReturnOverrideStatus.EXPIRED
            GlobalLeaseReturnOverrideStatus.GRANTED -> {
                when (next) {
                    GlobalLeaseReturnOverrideStatus.REVOKED, GlobalLeaseReturnOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalLeaseReturnOverrideStatus.REVOKED -> return next == GlobalLeaseReturnOverrideStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalLeaseReturnOverrideStatus) {
        if (!canMoveTo(next)) {
            error("global lease return override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): GlobalLeaseReturnOverride {
        return GlobalLeaseReturnOverride(
            code = code,
            label = label,
            phone = phone,
            summary = summary,
            graceDays = graceDays,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global lease return override in status requested. */
fun newGlobalLeaseReturnOverride(code: String, label: String): GlobalLeaseReturnOverride {
    return GlobalLeaseReturnOverride(
        code = code,
        label = label,
        phone = "",
        summary = "",
        graceDays = 0,
        attributes = mutableMapOf<String, String>(),
        status = GlobalLeaseReturnOverrideStatus.REQUESTED,
    )
}
