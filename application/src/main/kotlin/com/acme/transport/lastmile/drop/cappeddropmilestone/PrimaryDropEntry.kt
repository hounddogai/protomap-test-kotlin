package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.text.normalize

/** Where a primary drop entry is in its life cycle. */
enum class PrimaryDropEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary drop entry as the drop team keeps it. */
data class PrimaryDropEntry(
    var code: String,
    var title: String,
    var approved: Boolean,
    var contactEmail: String,
    var attributes: MutableMap<String, String>,
    var etaMinutes: Int,
    var status: PrimaryDropEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDropEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDropEntryStatus): Boolean {
        when (status) {
            PrimaryDropEntryStatus.PROPOSED -> {
                when (next) {
                    PrimaryDropEntryStatus.APPROVED, PrimaryDropEntryStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryDropEntryStatus.APPROVED -> {
                when (next) {
                    PrimaryDropEntryStatus.REJECTED, PrimaryDropEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryDropEntryStatus.REJECTED -> return next == PrimaryDropEntryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryDropEntryStatus) {
        if (!canMoveTo(next)) {
            error("primary drop entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): PrimaryDropEntry {
        return PrimaryDropEntry(
            code = code,
            title = title,
            approved = approved,
            contactEmail = contactEmail,
            attributes = attributes,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary drop entry in status proposed. */
fun newPrimaryDropEntry(code: String, title: String): PrimaryDropEntry {
    return PrimaryDropEntry(
        code = code,
        title = title,
        approved = false,
        contactEmail = "",
        attributes = mutableMapOf<String, String>(),
        etaMinutes = 0,
        status = PrimaryDropEntryStatus.PROPOSED,
    )
}
