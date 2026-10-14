package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled transit time batch records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a scheduled transit time batch is in its life cycle. */
enum class ScheduledTransitTimeBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled transit time batch. Its code is unique within a tenant. */
data class ScheduledTransitTimeBatch(
    var code: String,
    var name: String,
    var phone: String,
    var sequence: Int,
    var quantity: Int,
    var score: Double,
    var ratio: Double,
    var attempts: Int,
    var archived: Boolean,
    var startsAt: Int,
    var distanceKm: Double,
    var status: ScheduledTransitTimeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTransitTimeBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTransitTimeBatchStatus): Boolean {
        when (status) {
            ScheduledTransitTimeBatchStatus.OPEN -> return next == ScheduledTransitTimeBatchStatus.REOPENED
            ScheduledTransitTimeBatchStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledTransitTimeBatchStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledTransitTimeBatchStatus.RESOLVED -> {
                when (next) {
                    ScheduledTransitTimeBatchStatus.REOPENED, ScheduledTransitTimeBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledTransitTimeBatchStatus.REOPENED -> return next == ScheduledTransitTimeBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledTransitTimeBatchStatus) {
        if (!canMoveTo(next)) {
            error("scheduled transit time batch $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round((attempts.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): ScheduledTransitTimeBatch {
        return ScheduledTransitTimeBatch(
            code = code,
            name = name,
            phone = phone,
            sequence = sequence,
            quantity = quantity,
            score = score,
            ratio = ratio,
            attempts = attempts,
            archived = archived,
            startsAt = startsAt,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled transit time batch in status open. */
fun newScheduledTransitTimeBatch(code: String, name: String): ScheduledTransitTimeBatch {
    return ScheduledTransitTimeBatch(
        code = code,
        name = name,
        phone = "",
        sequence = 0,
        quantity = 0,
        score = 0.0,
        ratio = 0.0,
        attempts = 0,
        archived = false,
        startsAt = 0,
        distanceKm = 0.0,
        status = ScheduledTransitTimeBatchStatus.OPEN,
    )
}
