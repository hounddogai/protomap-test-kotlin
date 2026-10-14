package com.acme.transport.safety.cameraevent.group

import com.acme.transport.kit.text.normalize

/** Where a seasonal camera event entry is in its life cycle. */
enum class SeasonalCameraEventEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal camera event entry as the camera event team keeps it. */
data class SeasonalCameraEventEntry(
    var code: String,
    var name: String,
    var ownerId: String,
    var endsAt: Int,
    var tags: MutableList<String>,
    var externalRef: String,
    var summary: String,
    var status: SeasonalCameraEventEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCameraEventEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCameraEventEntryStatus): Boolean {
        when (status) {
            SeasonalCameraEventEntryStatus.PROPOSED -> return next == SeasonalCameraEventEntryStatus.REJECTED || next == SeasonalCameraEventEntryStatus.WITHDRAWN
            SeasonalCameraEventEntryStatus.APPROVED -> {
                when (next) {
                    SeasonalCameraEventEntryStatus.REJECTED -> return true
                }
                return false
            }
            SeasonalCameraEventEntryStatus.REJECTED -> return next == SeasonalCameraEventEntryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SeasonalCameraEventEntryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal camera event entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal camera event entry"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): SeasonalCameraEventEntry {
        return SeasonalCameraEventEntry(
            code = code,
            name = name,
            ownerId = ownerId,
            endsAt = endsAt,
            tags = tags,
            externalRef = externalRef,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal camera event entry in status proposed. */
fun newSeasonalCameraEventEntry(code: String, name: String): SeasonalCameraEventEntry {
    return SeasonalCameraEventEntry(
        code = code,
        name = name,
        ownerId = "",
        endsAt = 0,
        tags = mutableListOf<String>(),
        externalRef = "",
        summary = "",
        status = SeasonalCameraEventEntryStatus.PROPOSED,
    )
}
