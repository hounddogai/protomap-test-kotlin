package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.text.normalize

/** Where a legacy tire draft is in its life cycle. */
enum class LegacyTireDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A legacy tire draft. Its code is unique within a tenant. */
data class LegacyTireDraft(
    var code: String,
    var title: String,
    var summary: String,
    var priority: Int,
    var source: String,
    var tags: MutableList<String>,
    var status: LegacyTireDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTireDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTireDraftStatus): Boolean {
        when (status) {
            LegacyTireDraftStatus.OPEN -> return next == LegacyTireDraftStatus.IN_REVIEW || next == LegacyTireDraftStatus.RESOLVED
            LegacyTireDraftStatus.IN_REVIEW -> {
                when (next) {
                    LegacyTireDraftStatus.REOPENED -> return true
                }
                return false
            }
            LegacyTireDraftStatus.RESOLVED -> return next == LegacyTireDraftStatus.REOPENED || next == LegacyTireDraftStatus.ARCHIVED
            LegacyTireDraftStatus.REOPENED -> {
                when (next) {
                    LegacyTireDraftStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTireDraftStatus) {
        if (!canMoveTo(next)) {
            error("legacy tire draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy tire draft"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): LegacyTireDraft {
        return LegacyTireDraft(
            code = code,
            title = title,
            summary = summary,
            priority = priority,
            source = source,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a legacy tire draft in status open. */
fun newLegacyTireDraft(code: String, title: String): LegacyTireDraft {
    return LegacyTireDraft(
        code = code,
        title = title,
        summary = "",
        priority = 0,
        source = "",
        tags = mutableListOf<String>(),
        status = LegacyTireDraftStatus.OPEN,
    )
}
