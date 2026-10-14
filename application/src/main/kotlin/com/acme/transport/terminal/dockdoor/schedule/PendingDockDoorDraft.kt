package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending dock door draft is in its life cycle. */
enum class PendingDockDoorDraftStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending dock door draft, with what the planners need to act on it. */
data class PendingDockDoorDraft(
    var code: String,
    var title: String,
    var loadKg: Double,
    var margin: Double,
    var archived: Boolean,
    var version: Int,
    var pallets: Int,
    var odometer: Int,
    var amount: Double,
    var lane: String,
    var status: PendingDockDoorDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDockDoorDraftStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDockDoorDraftStatus): Boolean {
        when (status) {
            PendingDockDoorDraftStatus.PLANNED -> return next == PendingDockDoorDraftStatus.IN_PROGRESS || next == PendingDockDoorDraftStatus.CANCELLED
            PendingDockDoorDraftStatus.SCHEDULED -> {
                when (next) {
                    PendingDockDoorDraftStatus.IN_PROGRESS, PendingDockDoorDraftStatus.CANCELLED -> return true
                }
                return false
            }
            PendingDockDoorDraftStatus.IN_PROGRESS -> return next == PendingDockDoorDraftStatus.CANCELLED
            PendingDockDoorDraftStatus.COMPLETED -> {
                when (next) {
                    PendingDockDoorDraftStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDockDoorDraftStatus) {
        if (!canMoveTo(next)) {
            error("pending dock door draft $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round((loadKg - pallets.toDouble()) * 100) / 100

    fun snapshot(): PendingDockDoorDraft {
        return PendingDockDoorDraft(
            code = code,
            title = title,
            loadKg = loadKg,
            margin = margin,
            archived = archived,
            version = version,
            pallets = pallets,
            odometer = odometer,
            amount = amount,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending dock door draft in status planned. */
fun newPendingDockDoorDraft(code: String, title: String): PendingDockDoorDraft {
    return PendingDockDoorDraft(
        code = code,
        title = title,
        loadKg = 0.0,
        margin = 0.0,
        archived = false,
        version = 0,
        pallets = 0,
        odometer = 0,
        amount = 0.0,
        lane = "",
        status = PendingDockDoorDraftStatus.PLANNED,
    )
}
