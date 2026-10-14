package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict rest break draft is in its life cycle. */
enum class StrictRestBreakDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One strict rest break draft, with what the planners need to act on it. */
data class StrictRestBreakDraft(
    var code: String,
    var name: String,
    var sequence: Int,
    var floorValue: Double,
    var score: Double,
    var vehicleId: String,
    var segment: String,
    var status: StrictRestBreakDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRestBreakDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRestBreakDraftStatus): Boolean {
        when (status) {
            StrictRestBreakDraftStatus.REQUESTED -> {
                when (next) {
                    StrictRestBreakDraftStatus.REVOKED, StrictRestBreakDraftStatus.EXPIRED -> return true
                }
                return false
            }
            StrictRestBreakDraftStatus.GRANTED -> {
                when (next) {
                    StrictRestBreakDraftStatus.REVOKED, StrictRestBreakDraftStatus.EXPIRED -> return true
                }
                return false
            }
            StrictRestBreakDraftStatus.REVOKED -> return next == StrictRestBreakDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictRestBreakDraftStatus) {
        if (!canMoveTo(next)) {
            error("strict rest break draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netTotal(): Double = round((score + sequence.toDouble()) * 100) / 100

    fun snapshot(): StrictRestBreakDraft {
        return StrictRestBreakDraft(
            code = code,
            name = name,
            sequence = sequence,
            floorValue = floorValue,
            score = score,
            vehicleId = vehicleId,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict rest break draft in status requested. */
fun newStrictRestBreakDraft(code: String, name: String): StrictRestBreakDraft {
    return StrictRestBreakDraft(
        code = code,
        name = name,
        sequence = 0,
        floorValue = 0.0,
        score = 0.0,
        vehicleId = "",
        segment = "",
        status = StrictRestBreakDraftStatus.REQUESTED,
    )
}
