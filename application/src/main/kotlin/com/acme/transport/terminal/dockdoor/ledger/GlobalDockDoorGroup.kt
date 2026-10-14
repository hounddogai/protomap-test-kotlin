package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global dock door group is in its life cycle. */
enum class GlobalDockDoorGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One global dock door group, with what the planners need to act on it. */
data class GlobalDockDoorGroup(
    var code: String,
    var name: String,
    var priority: Int,
    var ceiling: Double,
    var startsAt: Int,
    var visible: Boolean,
    var approved: Boolean,
    var tags: MutableList<String>,
    var depot: String,
    var budget: Double,
    var updatedAt: Int,
    var status: GlobalDockDoorGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDockDoorGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDockDoorGroupStatus): Boolean {
        when (status) {
            GlobalDockDoorGroupStatus.OPEN -> return next == GlobalDockDoorGroupStatus.ARCHIVED
            GlobalDockDoorGroupStatus.IN_REVIEW -> return next == GlobalDockDoorGroupStatus.REOPENED || next == GlobalDockDoorGroupStatus.ARCHIVED
            GlobalDockDoorGroupStatus.RESOLVED -> {
                when (next) {
                    GlobalDockDoorGroupStatus.REOPENED, GlobalDockDoorGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalDockDoorGroupStatus.REOPENED -> return next == GlobalDockDoorGroupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalDockDoorGroupStatus) {
        if (!canMoveTo(next)) {
            error("global dock door group $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round((budget - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalDockDoorGroup {
        return GlobalDockDoorGroup(
            code = code,
            name = name,
            priority = priority,
            ceiling = ceiling,
            startsAt = startsAt,
            visible = visible,
            approved = approved,
            tags = tags,
            depot = depot,
            budget = budget,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global dock door group in status open. */
fun newGlobalDockDoorGroup(code: String, name: String): GlobalDockDoorGroup {
    return GlobalDockDoorGroup(
        code = code,
        name = name,
        priority = 0,
        ceiling = 0.0,
        startsAt = 0,
        visible = false,
        approved = false,
        tags = mutableListOf<String>(),
        depot = "",
        budget = 0.0,
        updatedAt = 0,
        status = GlobalDockDoorGroupStatus.OPEN,
    )
}
