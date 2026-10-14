package com.acme.transport.terminal.crossdock.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock entry is in its life cycle. */
enum class CrossDockEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a cross dock entry and its current status. */
data class CrossDockEntry(
    var code: String,
    var title: String,
    var pallets: Int,
    var summary: String,
    var contactEmail: String,
    var version: Int,
    var status: CrossDockEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockEntryStatus): Boolean {
        when (status) {
            CrossDockEntryStatus.DRAFT -> return next == CrossDockEntryStatus.PENDING
            CrossDockEntryStatus.PENDING -> {
                when (next) {
                    CrossDockEntryStatus.CLOSED -> return true
                }
                return false
            }
            CrossDockEntryStatus.ACTIVE -> {
                when (next) {
                    CrossDockEntryStatus.SUSPENDED -> return true
                }
                return false
            }
            CrossDockEntryStatus.SUSPENDED -> {
                when (next) {
                    CrossDockEntryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockEntryStatus) {
        if (!canMoveTo(next)) {
            error("cross dock entry $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"cross dock entry"} $code"

    fun weightedLoad(): Double = round((version.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): CrossDockEntry {
        return CrossDockEntry(
            code = code,
            title = title,
            pallets = pallets,
            summary = summary,
            contactEmail = contactEmail,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cross dock entry in status draft. */
fun newCrossDockEntry(code: String, title: String): CrossDockEntry {
    return CrossDockEntry(
        code = code,
        title = title,
        pallets = 0,
        summary = "",
        contactEmail = "",
        version = 0,
        status = CrossDockEntryStatus.DRAFT,
    )
}
