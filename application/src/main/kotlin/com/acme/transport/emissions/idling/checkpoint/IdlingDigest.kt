package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling digest is in its life cycle. */
enum class IdlingDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a idling digest and its current status. */
data class IdlingDigest(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var notes: String,
    var limit: Int,
    var startsAt: Int,
    var threshold: Double,
    var status: IdlingDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingDigestStatus): Boolean {
        when (status) {
            IdlingDigestStatus.CREATED -> {
                when (next) {
                    IdlingDigestStatus.VALIDATED, IdlingDigestStatus.RETIRED -> return true
                }
                return false
            }
            IdlingDigestStatus.VALIDATED -> {
                when (next) {
                    IdlingDigestStatus.PUBLISHED -> return true
                }
                return false
            }
            IdlingDigestStatus.PUBLISHED -> {
                when (next) {
                    IdlingDigestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingDigestStatus) {
        if (!canMoveTo(next)) {
            error("idling digest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"idling digest"} $code"

    fun netTotal(): Double = round(threshold * startsAt.toDouble() * 100) / 100

    fun snapshot(): IdlingDigest {
        return IdlingDigest(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            notes = notes,
            limit = limit,
            startsAt = startsAt,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a idling digest in status created. */
fun newIdlingDigest(code: String, label: String): IdlingDigest {
    return IdlingDigest(
        code = code,
        label = label,
        durationMinutes = 0,
        notes = "",
        limit = 0,
        startsAt = 0,
        threshold = 0.0,
        status = IdlingDigestStatus.CREATED,
    )
}
