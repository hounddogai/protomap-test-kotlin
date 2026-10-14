package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer yard manifest records of terminal's trailer yard work, and the rules, stores, and calculations
// around them.

/** Where a trailer yard manifest is in its life cycle. */
enum class TrailerYardManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A trailer yard manifest as the trailer yard team keeps it. */
data class TrailerYardManifest(
    var code: String,
    var title: String,
    var graceDays: Int,
    var priority: Int,
    var trailerId: String,
    var cost: Double,
    var contactEmail: String,
    var status: TrailerYardManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardManifestStatus): Boolean {
        when (status) {
            TrailerYardManifestStatus.IDLE -> {
                when (next) {
                    TrailerYardManifestStatus.DRAINING -> return true
                }
                return false
            }
            TrailerYardManifestStatus.WARMING -> {
                when (next) {
                    TrailerYardManifestStatus.READY, TrailerYardManifestStatus.STOPPED -> return true
                }
                return false
            }
            TrailerYardManifestStatus.READY -> return next == TrailerYardManifestStatus.DRAINING || next == TrailerYardManifestStatus.STOPPED
            TrailerYardManifestStatus.DRAINING -> return next == TrailerYardManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TrailerYardManifestStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round(priority.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): TrailerYardManifest {
        return TrailerYardManifest(
            code = code,
            title = title,
            graceDays = graceDays,
            priority = priority,
            trailerId = trailerId,
            cost = cost,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer yard manifest in status idle. */
fun newTrailerYardManifest(code: String, title: String): TrailerYardManifest {
    return TrailerYardManifest(
        code = code,
        title = title,
        graceDays = 0,
        priority = 0,
        trailerId = "",
        cost = 0.0,
        contactEmail = "",
        status = TrailerYardManifestStatus.IDLE,
    )
}
