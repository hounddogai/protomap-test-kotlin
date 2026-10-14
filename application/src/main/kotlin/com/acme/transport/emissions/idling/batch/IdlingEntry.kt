package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.text.normalize

/** Where a idling entry is in its life cycle. */
enum class IdlingEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A idling entry as the idling team keeps it. */
data class IdlingEntry(
    var code: String,
    var name: String,
    var description: String,
    var contactEmail: String,
    var category: String,
    var windowDays: Int,
    var summary: String,
    var status: IdlingEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingEntryStatus): Boolean {
        when (status) {
            IdlingEntryStatus.CREATED -> {
                when (next) {
                    IdlingEntryStatus.VALIDATED -> return true
                }
                return false
            }
            IdlingEntryStatus.VALIDATED -> {
                when (next) {
                    IdlingEntryStatus.RETIRED -> return true
                }
                return false
            }
            IdlingEntryStatus.PUBLISHED -> {
                when (next) {
                    IdlingEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingEntryStatus) {
        if (!canMoveTo(next)) {
            error("idling entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): IdlingEntry {
        return IdlingEntry(
            code = code,
            name = name,
            description = description,
            contactEmail = contactEmail,
            category = category,
            windowDays = windowDays,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a idling entry in status created. */
fun newIdlingEntry(code: String, name: String): IdlingEntry {
    return IdlingEntry(
        code = code,
        name = name,
        description = "",
        contactEmail = "",
        category = "",
        windowDays = 0,
        summary = "",
        status = IdlingEntryStatus.CREATED,
    )
}
