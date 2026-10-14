package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer profile is in its life cycle. */
enum class TrailerProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One trailer profile, with what the planners need to act on it. */
data class TrailerProfile(
    var code: String,
    var name: String,
    var stopCount: Int,
    var margin: Double,
    var graceDays: Int,
    var cost: Double,
    var summary: String,
    var phone: String,
    var dock: String,
    var country: String,
    var status: TrailerProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerProfileStatus): Boolean {
        when (status) {
            TrailerProfileStatus.DRAFT -> return next == TrailerProfileStatus.SUSPENDED
            TrailerProfileStatus.PENDING -> {
                when (next) {
                    TrailerProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            TrailerProfileStatus.ACTIVE -> {
                when (next) {
                    TrailerProfileStatus.CLOSED -> return true
                }
                return false
            }
            TrailerProfileStatus.SUSPENDED -> return next == TrailerProfileStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TrailerProfileStatus) {
        if (!canMoveTo(next)) {
            error("trailer profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedExposure(): Double = round(margin * cost * 100) / 100

    fun snapshot(): TrailerProfile {
        return TrailerProfile(
            code = code,
            name = name,
            stopCount = stopCount,
            margin = margin,
            graceDays = graceDays,
            cost = cost,
            summary = summary,
            phone = phone,
            dock = dock,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a trailer profile in status draft. */
fun newTrailerProfile(code: String, name: String): TrailerProfile {
    return TrailerProfile(
        code = code,
        name = name,
        stopCount = 0,
        margin = 0.0,
        graceDays = 0,
        cost = 0.0,
        summary = "",
        phone = "",
        dock = "",
        country = "",
        status = TrailerProfileStatus.DRAFT,
    )
}
