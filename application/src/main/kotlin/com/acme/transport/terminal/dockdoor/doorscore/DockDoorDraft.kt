package com.acme.transport.terminal.dockdoor.doorscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door draft is in its life cycle. */
enum class DockDoorDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a dock door draft and its current status. */
data class DockDoorDraft(
    var code: String,
    var name: String,
    var phone: String,
    var rate: Double,
    var createdAt: Int,
    var score: Double,
    var startsAt: Int,
    var status: DockDoorDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorDraftStatus): Boolean {
        when (status) {
            DockDoorDraftStatus.NEW -> return next == DockDoorDraftStatus.RUNNING || next == DockDoorDraftStatus.DONE
            DockDoorDraftStatus.QUEUED -> return next == DockDoorDraftStatus.RUNNING || next == DockDoorDraftStatus.DONE
            DockDoorDraftStatus.RUNNING -> {
                when (next) {
                    DockDoorDraftStatus.DONE, DockDoorDraftStatus.FAILED -> return true
                }
                return false
            }
            DockDoorDraftStatus.DONE -> return next == DockDoorDraftStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DockDoorDraftStatus) {
        if (!canMoveTo(next)) {
            error("dock door draft $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((startsAt.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): DockDoorDraft {
        return DockDoorDraft(
            code = code,
            name = name,
            phone = phone,
            rate = rate,
            createdAt = createdAt,
            score = score,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dock door draft in status new. */
fun newDockDoorDraft(code: String, name: String): DockDoorDraft {
    return DockDoorDraft(
        code = code,
        name = name,
        phone = "",
        rate = 0.0,
        createdAt = 0,
        score = 0.0,
        startsAt = 0,
        status = DockDoorDraftStatus.NEW,
    )
}
