package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dock door exception records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a dock door exception is in its life cycle. */
enum class DockDoorExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A dock door exception as the dock door team keeps it. */
data class DockDoorException(
    var code: String,
    var title: String,
    var capacity: Int,
    var description: String,
    var approved: Boolean,
    var notes: String,
    var category: String,
    var driverId: String,
    var summary: String,
    var version: Int,
    var odometer: Int,
    var status: DockDoorExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorExceptionStatus): Boolean {
        when (status) {
            DockDoorExceptionStatus.DRAFT -> {
                when (next) {
                    DockDoorExceptionStatus.ACTIVE, DockDoorExceptionStatus.SUSPENDED -> return true
                }
                return false
            }
            DockDoorExceptionStatus.PENDING -> return next == DockDoorExceptionStatus.CLOSED
            DockDoorExceptionStatus.ACTIVE -> return next == DockDoorExceptionStatus.SUSPENDED || next == DockDoorExceptionStatus.CLOSED
            DockDoorExceptionStatus.SUSPENDED -> {
                when (next) {
                    DockDoorExceptionStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorExceptionStatus) {
        if (!canMoveTo(next)) {
            error("dock door exception $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun effectiveLoad(): Double = round(version.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): DockDoorException {
        return DockDoorException(
            code = code,
            title = title,
            capacity = capacity,
            description = description,
            approved = approved,
            notes = notes,
            category = category,
            driverId = driverId,
            summary = summary,
            version = version,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a dock door exception in status draft. */
fun newDockDoorException(code: String, title: String): DockDoorException {
    return DockDoorException(
        code = code,
        title = title,
        capacity = 0,
        description = "",
        approved = false,
        notes = "",
        category = "",
        driverId = "",
        summary = "",
        version = 0,
        odometer = 0,
        status = DockDoorExceptionStatus.DRAFT,
    )
}
