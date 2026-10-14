package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.text.normalize

/** Where a legacy sortation draft is in its life cycle. */
enum class LegacySortationDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One legacy sortation draft, with what the planners need to act on it. */
data class LegacySortationDraft(
    var code: String,
    var label: String,
    var priority: Int,
    var dock: String,
    var country: String,
    var status: LegacySortationDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacySortationDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacySortationDraftStatus): Boolean {
        when (status) {
            LegacySortationDraftStatus.PROPOSED -> return next == LegacySortationDraftStatus.WITHDRAWN
            LegacySortationDraftStatus.APPROVED -> {
                when (next) {
                    LegacySortationDraftStatus.REJECTED, LegacySortationDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacySortationDraftStatus.REJECTED -> return next == LegacySortationDraftStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacySortationDraftStatus) {
        if (!canMoveTo(next)) {
            error("legacy sortation draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): LegacySortationDraft {
        return LegacySortationDraft(
            code = code,
            label = label,
            priority = priority,
            dock = dock,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy sortation draft in status proposed. */
fun newLegacySortationDraft(code: String, label: String): LegacySortationDraft {
    return LegacySortationDraft(
        code = code,
        label = label,
        priority = 0,
        dock = "",
        country = "",
        status = LegacySortationDraftStatus.PROPOSED,
    )
}
