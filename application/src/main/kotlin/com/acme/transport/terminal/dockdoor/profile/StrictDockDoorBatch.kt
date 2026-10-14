package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.kit.text.normalize

/** Where a strict dock door batch is in its life cycle. */
enum class StrictDockDoorBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict dock door batch as the dock door team keeps it. */
data class StrictDockDoorBatch(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var description: String,
    var notes: String,
    var amount: Double,
    var status: StrictDockDoorBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDockDoorBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDockDoorBatchStatus): Boolean {
        when (status) {
            StrictDockDoorBatchStatus.CREATED -> return next == StrictDockDoorBatchStatus.PUBLISHED
            StrictDockDoorBatchStatus.VALIDATED -> return next == StrictDockDoorBatchStatus.PUBLISHED
            StrictDockDoorBatchStatus.PUBLISHED -> {
                when (next) {
                    StrictDockDoorBatchStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDockDoorBatchStatus) {
        if (!canMoveTo(next)) {
            error("strict dock door batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict dock door batch"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = amount

    fun snapshot(): StrictDockDoorBatch {
        return StrictDockDoorBatch(
            code = code,
            title = title,
            attributes = attributes,
            description = description,
            notes = notes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict dock door batch in status created. */
fun newStrictDockDoorBatch(code: String, title: String): StrictDockDoorBatch {
    return StrictDockDoorBatch(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        description = "",
        notes = "",
        amount = 0.0,
        status = StrictDockDoorBatchStatus.CREATED,
    )
}
