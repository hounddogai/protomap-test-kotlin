package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted attempt profile records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a weighted attempt profile is in its life cycle. */
enum class WeightedAttemptProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted attempt profile as the attempt team keeps it. */
data class WeightedAttemptProfile(
    var code: String,
    var title: String,
    var startsAt: Int,
    var amount: Double,
    var carrier: String,
    var batchSize: Int,
    var sequence: Int,
    var quantity: Int,
    var externalRef: String,
    var dock: String,
    var segment: String,
    var status: WeightedAttemptProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAttemptProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAttemptProfileStatus): Boolean {
        when (status) {
            WeightedAttemptProfileStatus.CREATED -> {
                when (next) {
                    WeightedAttemptProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            WeightedAttemptProfileStatus.VALIDATED -> return next == WeightedAttemptProfileStatus.PUBLISHED || next == WeightedAttemptProfileStatus.RETIRED
            WeightedAttemptProfileStatus.PUBLISHED -> {
                when (next) {
                    WeightedAttemptProfileStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedAttemptProfileStatus) {
        if (!canMoveTo(next)) {
            error("weighted attempt profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netValue(): Double = round((quantity.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): WeightedAttemptProfile {
        return WeightedAttemptProfile(
            code = code,
            title = title,
            startsAt = startsAt,
            amount = amount,
            carrier = carrier,
            batchSize = batchSize,
            sequence = sequence,
            quantity = quantity,
            externalRef = externalRef,
            dock = dock,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted attempt profile in status created. */
fun newWeightedAttemptProfile(code: String, title: String): WeightedAttemptProfile {
    return WeightedAttemptProfile(
        code = code,
        title = title,
        startsAt = 0,
        amount = 0.0,
        carrier = "",
        batchSize = 0,
        sequence = 0,
        quantity = 0,
        externalRef = "",
        dock = "",
        segment = "",
        status = WeightedAttemptProfileStatus.CREATED,
    )
}
