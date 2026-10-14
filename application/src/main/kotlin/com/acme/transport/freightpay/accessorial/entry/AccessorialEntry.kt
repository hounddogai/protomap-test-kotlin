package com.acme.transport.freightpay.accessorial.entry

import com.acme.transport.kit.text.normalize

// The accessorial entry records of freightpay's accessorial work, and the rules, stores, and calculations
// around them.

/** Where a accessorial entry is in its life cycle. */
enum class AccessorialEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A accessorial entry as the accessorial team keeps it. */
data class AccessorialEntry(
    var code: String,
    var title: String,
    var version: Int,
    var depot: String,
    var active: Boolean,
    var status: AccessorialEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialEntryStatus): Boolean {
        when (status) {
            AccessorialEntryStatus.OPEN -> {
                when (next) {
                    AccessorialEntryStatus.IN_REVIEW, AccessorialEntryStatus.RESOLVED -> return true
                }
                return false
            }
            AccessorialEntryStatus.IN_REVIEW -> {
                when (next) {
                    AccessorialEntryStatus.RESOLVED, AccessorialEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            AccessorialEntryStatus.RESOLVED -> {
                when (next) {
                    AccessorialEntryStatus.REOPENED, AccessorialEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            AccessorialEntryStatus.REOPENED -> {
                when (next) {
                    AccessorialEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialEntryStatus) {
        if (!canMoveTo(next)) {
            error("accessorial entry $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): AccessorialEntry {
        return AccessorialEntry(
            code = code,
            title = title,
            version = version,
            depot = depot,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a accessorial entry in status open. */
fun newAccessorialEntry(code: String, title: String): AccessorialEntry {
    return AccessorialEntry(
        code = code,
        title = title,
        version = 0,
        depot = "",
        active = false,
        status = AccessorialEntryStatus.OPEN,
    )
}
