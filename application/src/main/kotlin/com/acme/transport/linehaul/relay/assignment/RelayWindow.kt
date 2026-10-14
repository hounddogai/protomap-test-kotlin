package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay window is in its life cycle. */
enum class RelayWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A relay window as the relay team keeps it. */
data class RelayWindow(
    var code: String,
    var title: String,
    var notes: String,
    var archived: Boolean,
    var reviewer: String,
    var rate: Double,
    var loadKg: Double,
    var durationMinutes: Int,
    var country: String,
    var endsAt: Int,
    var active: Boolean,
    var status: RelayWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayWindowStatus): Boolean {
        when (status) {
            RelayWindowStatus.OPEN -> return next == RelayWindowStatus.RESOLVED || next == RelayWindowStatus.REOPENED
            RelayWindowStatus.IN_REVIEW -> return next == RelayWindowStatus.REOPENED || next == RelayWindowStatus.ARCHIVED
            RelayWindowStatus.RESOLVED -> return next == RelayWindowStatus.REOPENED || next == RelayWindowStatus.ARCHIVED
            RelayWindowStatus.REOPENED -> {
                when (next) {
                    RelayWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayWindowStatus) {
        if (!canMoveTo(next)) {
            error("relay window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"relay window"} $code"

    fun netValue(): Double = round((loadKg - endsAt.toDouble()) * 100) / 100

    fun snapshot(): RelayWindow {
        return RelayWindow(
            code = code,
            title = title,
            notes = notes,
            archived = archived,
            reviewer = reviewer,
            rate = rate,
            loadKg = loadKg,
            durationMinutes = durationMinutes,
            country = country,
            endsAt = endsAt,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a relay window in status open. */
fun newRelayWindow(code: String, title: String): RelayWindow {
    return RelayWindow(
        code = code,
        title = title,
        notes = "",
        archived = false,
        reviewer = "",
        rate = 0.0,
        loadKg = 0.0,
        durationMinutes = 0,
        country = "",
        endsAt = 0,
        active = false,
        status = RelayWindowStatus.OPEN,
    )
}
