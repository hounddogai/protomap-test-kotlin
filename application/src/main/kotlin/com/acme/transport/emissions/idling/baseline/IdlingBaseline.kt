package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The idling baseline records of emissions's idling work, and the rules, stores, and calculations around
// them.

/** Where a idling baseline is in its life cycle. */
enum class IdlingBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A idling baseline. Its code is unique within a tenant. */
data class IdlingBaseline(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var archived: Boolean,
    var routeCode: String,
    var expiresAt: Int,
    var capacity: Int,
    var score: Double,
    var channel: String,
    var graceDays: Int,
    var status: IdlingBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingBaselineStatus): Boolean {
        when (status) {
            IdlingBaselineStatus.DRAFT -> {
                when (next) {
                    IdlingBaselineStatus.PENDING, IdlingBaselineStatus.ACTIVE -> return true
                }
                return false
            }
            IdlingBaselineStatus.PENDING -> return next == IdlingBaselineStatus.ACTIVE || next == IdlingBaselineStatus.SUSPENDED
            IdlingBaselineStatus.ACTIVE -> return next == IdlingBaselineStatus.CLOSED
            IdlingBaselineStatus.SUSPENDED -> {
                when (next) {
                    IdlingBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingBaselineStatus) {
        if (!canMoveTo(next)) {
            error("idling baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round((capacity.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): IdlingBaseline {
        return IdlingBaseline(
            code = code,
            label = label,
            distanceKm = distanceKm,
            archived = archived,
            routeCode = routeCode,
            expiresAt = expiresAt,
            capacity = capacity,
            score = score,
            channel = channel,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a idling baseline in status draft. */
fun newIdlingBaseline(code: String, label: String): IdlingBaseline {
    return IdlingBaseline(
        code = code,
        label = label,
        distanceKm = 0.0,
        archived = false,
        routeCode = "",
        expiresAt = 0,
        capacity = 0,
        score = 0.0,
        channel = "",
        graceDays = 0,
        status = IdlingBaselineStatus.DRAFT,
    )
}
