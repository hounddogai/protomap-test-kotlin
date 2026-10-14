package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global sortation milestone is in its life cycle. */
enum class GlobalSortationMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One global sortation milestone, with what the planners need to act on it. */
data class GlobalSortationMilestone(
    var code: String,
    var name: String,
    var unit: String,
    var createdAt: Int,
    var margin: Double,
    var status: GlobalSortationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalSortationMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalSortationMilestoneStatus): Boolean {
        when (status) {
            GlobalSortationMilestoneStatus.NEW -> return next == GlobalSortationMilestoneStatus.RUNNING || next == GlobalSortationMilestoneStatus.DONE
            GlobalSortationMilestoneStatus.QUEUED -> {
                when (next) {
                    GlobalSortationMilestoneStatus.FAILED -> return true
                }
                return false
            }
            GlobalSortationMilestoneStatus.RUNNING -> {
                when (next) {
                    GlobalSortationMilestoneStatus.DONE -> return true
                }
                return false
            }
            GlobalSortationMilestoneStatus.DONE -> return next == GlobalSortationMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalSortationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("global sortation milestone $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round((margin - createdAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalSortationMilestone {
        return GlobalSortationMilestone(
            code = code,
            name = name,
            unit = unit,
            createdAt = createdAt,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a global sortation milestone in status new. */
fun newGlobalSortationMilestone(code: String, name: String): GlobalSortationMilestone {
    return GlobalSortationMilestone(
        code = code,
        name = name,
        unit = "",
        createdAt = 0,
        margin = 0.0,
        status = GlobalSortationMilestoneStatus.NEW,
    )
}
