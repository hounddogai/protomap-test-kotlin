package com.acme.transport.terminal.traileryard.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard entry is in its life cycle. */
enum class TrailerYardEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One trailer yard entry, with what the planners need to act on it. */
data class TrailerYardEntry(
    var code: String,
    var name: String,
    var contactEmail: String,
    var capacity: Int,
    var revision: Int,
    var trailerId: String,
    var etaMinutes: Int,
    var lane: String,
    var score: Double,
    var status: TrailerYardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardEntryStatus): Boolean {
        when (status) {
            TrailerYardEntryStatus.CREATED -> return next == TrailerYardEntryStatus.PUBLISHED
            TrailerYardEntryStatus.VALIDATED -> {
                when (next) {
                    TrailerYardEntryStatus.PUBLISHED -> return true
                }
                return false
            }
            TrailerYardEntryStatus.PUBLISHED -> {
                when (next) {
                    TrailerYardEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardEntryStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard entry $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun adjustedVolume(): Double = round((revision.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardEntry {
        return TrailerYardEntry(
            code = code,
            name = name,
            contactEmail = contactEmail,
            capacity = capacity,
            revision = revision,
            trailerId = trailerId,
            etaMinutes = etaMinutes,
            lane = lane,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a trailer yard entry in status created. */
fun newTrailerYardEntry(code: String, name: String): TrailerYardEntry {
    return TrailerYardEntry(
        code = code,
        name = name,
        contactEmail = "",
        capacity = 0,
        revision = 0,
        trailerId = "",
        etaMinutes = 0,
        lane = "",
        score = 0.0,
        status = TrailerYardEntryStatus.CREATED,
    )
}
