package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dock door mapping records of terminal's dock door work, and the rules, stores, and calculations around
// them.

/** Where a dock door mapping is in its life cycle. */
enum class DockDoorMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a dock door mapping and its current status. */
data class DockDoorMapping(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var segment: String,
    var title: String,
    var etaMinutes: Int,
    var version: Int,
    var endsAt: Int,
    var summary: String,
    var status: DockDoorMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorMappingStatus): Boolean {
        when (status) {
            DockDoorMappingStatus.DRAFT -> {
                when (next) {
                    DockDoorMappingStatus.ACTIVE -> return true
                }
                return false
            }
            DockDoorMappingStatus.PENDING -> return next == DockDoorMappingStatus.ACTIVE || next == DockDoorMappingStatus.SUSPENDED
            DockDoorMappingStatus.ACTIVE -> {
                when (next) {
                    DockDoorMappingStatus.SUSPENDED, DockDoorMappingStatus.CLOSED -> return true
                }
                return false
            }
            DockDoorMappingStatus.SUSPENDED -> return next == DockDoorMappingStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DockDoorMappingStatus) {
        if (!canMoveTo(next)) {
            error("dock door mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"dock door mapping"} $code"

    fun effectiveTotal(): Double = round(etaMinutes.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): DockDoorMapping {
        return DockDoorMapping(
            code = code,
            name = name,
            pinned = pinned,
            segment = segment,
            title = title,
            etaMinutes = etaMinutes,
            version = version,
            endsAt = endsAt,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a dock door mapping in status draft. */
fun newDockDoorMapping(code: String, name: String): DockDoorMapping {
    return DockDoorMapping(
        code = code,
        name = name,
        pinned = false,
        segment = "",
        title = "",
        etaMinutes = 0,
        version = 0,
        endsAt = 0,
        summary = "",
        status = DockDoorMappingStatus.DRAFT,
    )
}
