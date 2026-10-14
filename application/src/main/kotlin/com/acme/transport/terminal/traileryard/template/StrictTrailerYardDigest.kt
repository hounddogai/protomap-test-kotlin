package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict trailer yard digest is in its life cycle. */
enum class StrictTrailerYardDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a strict trailer yard digest and its current status. */
data class StrictTrailerYardDigest(
    var code: String,
    var name: String,
    var quantity: Int,
    var attempts: Int,
    var limit: Int,
    var summary: String,
    var batchSize: Int,
    var notes: String,
    var status: StrictTrailerYardDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTrailerYardDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTrailerYardDigestStatus): Boolean {
        when (status) {
            StrictTrailerYardDigestStatus.CREATED -> return next == StrictTrailerYardDigestStatus.RETIRED
            StrictTrailerYardDigestStatus.VALIDATED -> {
                when (next) {
                    StrictTrailerYardDigestStatus.PUBLISHED -> return true
                }
                return false
            }
            StrictTrailerYardDigestStatus.PUBLISHED -> {
                when (next) {
                    StrictTrailerYardDigestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTrailerYardDigestStatus) {
        if (!canMoveTo(next)) {
            error("strict trailer yard digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedVolume(): Double = round(quantity.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): StrictTrailerYardDigest {
        return StrictTrailerYardDigest(
            code = code,
            name = name,
            quantity = quantity,
            attempts = attempts,
            limit = limit,
            summary = summary,
            batchSize = batchSize,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict trailer yard digest in status created. */
fun newStrictTrailerYardDigest(code: String, name: String): StrictTrailerYardDigest {
    return StrictTrailerYardDigest(
        code = code,
        name = name,
        quantity = 0,
        attempts = 0,
        limit = 0,
        summary = "",
        batchSize = 0,
        notes = "",
        status = StrictTrailerYardDigestStatus.CREATED,
    )
}
