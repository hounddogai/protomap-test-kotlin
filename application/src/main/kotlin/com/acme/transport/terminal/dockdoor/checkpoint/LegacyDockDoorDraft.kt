package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy dock door draft is in its life cycle. */
enum class LegacyDockDoorDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One legacy dock door draft, with what the planners need to act on it. */
data class LegacyDockDoorDraft(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var endsAt: Int,
    var description: String,
    var label: String,
    var status: LegacyDockDoorDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDockDoorDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDockDoorDraftStatus): Boolean {
        when (status) {
            LegacyDockDoorDraftStatus.IDLE -> {
                when (next) {
                    LegacyDockDoorDraftStatus.DRAINING -> return true
                }
                return false
            }
            LegacyDockDoorDraftStatus.WARMING -> return next == LegacyDockDoorDraftStatus.READY || next == LegacyDockDoorDraftStatus.STOPPED
            LegacyDockDoorDraftStatus.READY -> {
                when (next) {
                    LegacyDockDoorDraftStatus.DRAINING, LegacyDockDoorDraftStatus.STOPPED -> return true
                }
                return false
            }
            LegacyDockDoorDraftStatus.DRAINING -> return next == LegacyDockDoorDraftStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyDockDoorDraftStatus) {
        if (!canMoveTo(next)) {
            error("legacy dock door draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy dock door draft"} $code"

    fun weightedValue(): Double = round((updatedAt.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyDockDoorDraft {
        return LegacyDockDoorDraft(
            code = code,
            title = title,
            updatedAt = updatedAt,
            endsAt = endsAt,
            description = description,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy dock door draft in status idle. */
fun newLegacyDockDoorDraft(code: String, title: String): LegacyDockDoorDraft {
    return LegacyDockDoorDraft(
        code = code,
        title = title,
        updatedAt = 0,
        endsAt = 0,
        description = "",
        label = "",
        status = LegacyDockDoorDraftStatus.IDLE,
    )
}
