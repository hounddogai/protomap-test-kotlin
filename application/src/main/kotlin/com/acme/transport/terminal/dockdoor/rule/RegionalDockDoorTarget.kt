package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional dock door target is in its life cycle. */
enum class RegionalDockDoorTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional dock door target and its current status. */
data class RegionalDockDoorTarget(
    var code: String,
    var title: String,
    var startsAt: Int,
    var margin: Double,
    var phone: String,
    var createdAt: Int,
    var status: RegionalDockDoorTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDockDoorTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDockDoorTargetStatus): Boolean {
        when (status) {
            RegionalDockDoorTargetStatus.CREATED -> return next == RegionalDockDoorTargetStatus.PUBLISHED || next == RegionalDockDoorTargetStatus.RETIRED
            RegionalDockDoorTargetStatus.VALIDATED -> return next == RegionalDockDoorTargetStatus.PUBLISHED
            RegionalDockDoorTargetStatus.PUBLISHED -> {
                when (next) {
                    RegionalDockDoorTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDockDoorTargetStatus) {
        if (!canMoveTo(next)) {
            error("regional dock door target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netTotal(): Double = round((startsAt.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalDockDoorTarget {
        return RegionalDockDoorTarget(
            code = code,
            title = title,
            startsAt = startsAt,
            margin = margin,
            phone = phone,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional dock door target in status created. */
fun newRegionalDockDoorTarget(code: String, title: String): RegionalDockDoorTarget {
    return RegionalDockDoorTarget(
        code = code,
        title = title,
        startsAt = 0,
        margin = 0.0,
        phone = "",
        createdAt = 0,
        status = RegionalDockDoorTargetStatus.CREATED,
    )
}
