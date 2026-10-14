package com.acme.transport.network.transittime.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default transit time group is in its life cycle. */
enum class DefaultTransitTimeGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a default transit time group and its current status. */
data class DefaultTransitTimeGroup(
    var code: String,
    var label: String,
    var dock: String,
    var windowDays: Int,
    var durationMinutes: Int,
    var margin: Double,
    var contactEmail: String,
    var priority: Int,
    var status: DefaultTransitTimeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTransitTimeGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTransitTimeGroupStatus): Boolean {
        when (status) {
            DefaultTransitTimeGroupStatus.DRAFT -> {
                when (next) {
                    DefaultTransitTimeGroupStatus.ACTIVE, DefaultTransitTimeGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultTransitTimeGroupStatus.PENDING -> {
                when (next) {
                    DefaultTransitTimeGroupStatus.ACTIVE, DefaultTransitTimeGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultTransitTimeGroupStatus.ACTIVE -> {
                when (next) {
                    DefaultTransitTimeGroupStatus.SUSPENDED, DefaultTransitTimeGroupStatus.CLOSED -> return true
                }
                return false
            }
            DefaultTransitTimeGroupStatus.SUSPENDED -> return next == DefaultTransitTimeGroupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultTransitTimeGroupStatus) {
        if (!canMoveTo(next)) {
            error("default transit time group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default transit time group"} $code"

    fun adjustedVolume(): Double = round(windowDays.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): DefaultTransitTimeGroup {
        return DefaultTransitTimeGroup(
            code = code,
            label = label,
            dock = dock,
            windowDays = windowDays,
            durationMinutes = durationMinutes,
            margin = margin,
            contactEmail = contactEmail,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default transit time group in status draft. */
fun newDefaultTransitTimeGroup(code: String, label: String): DefaultTransitTimeGroup {
    return DefaultTransitTimeGroup(
        code = code,
        label = label,
        dock = "",
        windowDays = 0,
        durationMinutes = 0,
        margin = 0.0,
        contactEmail = "",
        priority = 0,
        status = DefaultTransitTimeGroupStatus.DRAFT,
    )
}
