package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled trailer override is in its life cycle. */
enum class ScheduledTrailerOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a scheduled trailer override and its current status. */
data class ScheduledTrailerOverride(
    var code: String,
    var name: String,
    var loadKg: Double,
    var approved: Boolean,
    var routeCode: String,
    var score: Double,
    var status: ScheduledTrailerOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTrailerOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTrailerOverrideStatus): Boolean {
        when (status) {
            ScheduledTrailerOverrideStatus.NEW -> {
                when (next) {
                    ScheduledTrailerOverrideStatus.QUEUED -> return true
                }
                return false
            }
            ScheduledTrailerOverrideStatus.QUEUED -> return next == ScheduledTrailerOverrideStatus.DONE || next == ScheduledTrailerOverrideStatus.FAILED
            ScheduledTrailerOverrideStatus.RUNNING -> return next == ScheduledTrailerOverrideStatus.DONE
            ScheduledTrailerOverrideStatus.DONE -> {
                when (next) {
                    ScheduledTrailerOverrideStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledTrailerOverrideStatus) {
        if (!canMoveTo(next)) {
            error("scheduled trailer override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netTotal(): Double = round(score * loadKg * 100) / 100

    fun snapshot(): ScheduledTrailerOverride {
        return ScheduledTrailerOverride(
            code = code,
            name = name,
            loadKg = loadKg,
            approved = approved,
            routeCode = routeCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled trailer override in status new. */
fun newScheduledTrailerOverride(code: String, name: String): ScheduledTrailerOverride {
    return ScheduledTrailerOverride(
        code = code,
        name = name,
        loadKg = 0.0,
        approved = false,
        routeCode = "",
        score = 0.0,
        status = ScheduledTrailerOverrideStatus.NEW,
    )
}
