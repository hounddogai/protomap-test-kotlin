package com.acme.transport.linehaul.trailer.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer entry is in its life cycle. */
enum class TrailerEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One trailer entry, with what the planners need to act on it. */
data class TrailerEntry(
    var code: String,
    var name: String,
    var channel: String,
    var archived: Boolean,
    var category: String,
    var score: Double,
    var title: String,
    var ceiling: Double,
    var amount: Double,
    var status: TrailerEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerEntryStatus): Boolean {
        when (status) {
            TrailerEntryStatus.CREATED -> {
                when (next) {
                    TrailerEntryStatus.PUBLISHED, TrailerEntryStatus.RETIRED -> return true
                }
                return false
            }
            TrailerEntryStatus.VALIDATED -> {
                when (next) {
                    TrailerEntryStatus.PUBLISHED -> return true
                }
                return false
            }
            TrailerEntryStatus.PUBLISHED -> return next == TrailerEntryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TrailerEntryStatus) {
        if (!canMoveTo(next)) {
            error("trailer entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedLoad(): Double = round((amount + score) * 100) / 100

    fun snapshot(): TrailerEntry {
        return TrailerEntry(
            code = code,
            name = name,
            channel = channel,
            archived = archived,
            category = category,
            score = score,
            title = title,
            ceiling = ceiling,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a trailer entry in status created. */
fun newTrailerEntry(code: String, name: String): TrailerEntry {
    return TrailerEntry(
        code = code,
        name = name,
        channel = "",
        archived = false,
        category = "",
        score = 0.0,
        title = "",
        ceiling = 0.0,
        amount = 0.0,
        status = TrailerEntryStatus.CREATED,
    )
}
