package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.kit.text.normalize

// The global utilization entry records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a global utilization entry is in its life cycle. */
enum class GlobalUtilizationEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global utilization entry. Its code is unique within a tenant. */
data class GlobalUtilizationEntry(
    var code: String,
    var title: String,
    var carrier: String,
    var attributes: MutableMap<String, String>,
    var pallets: Int,
    var status: GlobalUtilizationEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalUtilizationEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalUtilizationEntryStatus): Boolean {
        when (status) {
            GlobalUtilizationEntryStatus.OPEN -> return next == GlobalUtilizationEntryStatus.IN_REVIEW || next == GlobalUtilizationEntryStatus.REOPENED
            GlobalUtilizationEntryStatus.IN_REVIEW -> {
                when (next) {
                    GlobalUtilizationEntryStatus.REOPENED, GlobalUtilizationEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalUtilizationEntryStatus.RESOLVED -> return next == GlobalUtilizationEntryStatus.REOPENED || next == GlobalUtilizationEntryStatus.ARCHIVED
            GlobalUtilizationEntryStatus.REOPENED -> {
                when (next) {
                    GlobalUtilizationEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalUtilizationEntryStatus) {
        if (!canMoveTo(next)) {
            error("global utilization entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): GlobalUtilizationEntry {
        return GlobalUtilizationEntry(
            code = code,
            title = title,
            carrier = carrier,
            attributes = attributes,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global utilization entry in status open. */
fun newGlobalUtilizationEntry(code: String, title: String): GlobalUtilizationEntry {
    return GlobalUtilizationEntry(
        code = code,
        title = title,
        carrier = "",
        attributes = mutableMapOf<String, String>(),
        pallets = 0,
        status = GlobalUtilizationEntryStatus.OPEN,
    )
}
