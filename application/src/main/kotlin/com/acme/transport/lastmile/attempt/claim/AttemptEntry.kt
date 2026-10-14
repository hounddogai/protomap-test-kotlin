package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.text.normalize

/** Where a attempt entry is in its life cycle. */
enum class AttemptEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A attempt entry as the attempt team keeps it. */
data class AttemptEntry(
    var code: String,
    var label: String,
    var archived: Boolean,
    var unit: String,
    var channel: String,
    var attributes: MutableMap<String, String>,
    var shiftCode: String,
    var contactEmail: String,
    var createdAt: Int,
    var segment: String,
    var notes: String,
    var status: AttemptEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptEntryStatus): Boolean {
        when (status) {
            AttemptEntryStatus.CREATED -> {
                when (next) {
                    AttemptEntryStatus.PUBLISHED -> return true
                }
                return false
            }
            AttemptEntryStatus.VALIDATED -> return next == AttemptEntryStatus.RETIRED
            AttemptEntryStatus.PUBLISHED -> {
                when (next) {
                    AttemptEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptEntryStatus) {
        if (!canMoveTo(next)) {
            error("attempt entry $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"attempt entry"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): AttemptEntry {
        return AttemptEntry(
            code = code,
            label = label,
            archived = archived,
            unit = unit,
            channel = channel,
            attributes = attributes,
            shiftCode = shiftCode,
            contactEmail = contactEmail,
            createdAt = createdAt,
            segment = segment,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a attempt entry in status created. */
fun newAttemptEntry(code: String, label: String): AttemptEntry {
    return AttemptEntry(
        code = code,
        label = label,
        archived = false,
        unit = "",
        channel = "",
        attributes = mutableMapOf<String, String>(),
        shiftCode = "",
        contactEmail = "",
        createdAt = 0,
        segment = "",
        notes = "",
        status = AttemptEntryStatus.CREATED,
    )
}
