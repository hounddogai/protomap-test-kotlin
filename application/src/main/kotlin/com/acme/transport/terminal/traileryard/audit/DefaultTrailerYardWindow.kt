package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default trailer yard window is in its life cycle. */
enum class DefaultTrailerYardWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One default trailer yard window, with what the planners need to act on it. */
data class DefaultTrailerYardWindow(
    var code: String,
    var label: String,
    var windowDays: Int,
    var driverId: String,
    var reviewer: String,
    var endsAt: Int,
    var contactEmail: String,
    var revision: Int,
    var margin: Double,
    var source: String,
    var dock: String,
    var status: DefaultTrailerYardWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTrailerYardWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTrailerYardWindowStatus): Boolean {
        when (status) {
            DefaultTrailerYardWindowStatus.IDLE -> {
                when (next) {
                    DefaultTrailerYardWindowStatus.WARMING -> return true
                }
                return false
            }
            DefaultTrailerYardWindowStatus.WARMING -> return next == DefaultTrailerYardWindowStatus.DRAINING || next == DefaultTrailerYardWindowStatus.STOPPED
            DefaultTrailerYardWindowStatus.READY -> return next == DefaultTrailerYardWindowStatus.DRAINING
            DefaultTrailerYardWindowStatus.DRAINING -> {
                when (next) {
                    DefaultTrailerYardWindowStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultTrailerYardWindowStatus) {
        if (!canMoveTo(next)) {
            error("default trailer yard window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round((margin + windowDays.toDouble()) * 100) / 100

    fun snapshot(): DefaultTrailerYardWindow {
        return DefaultTrailerYardWindow(
            code = code,
            label = label,
            windowDays = windowDays,
            driverId = driverId,
            reviewer = reviewer,
            endsAt = endsAt,
            contactEmail = contactEmail,
            revision = revision,
            margin = margin,
            source = source,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default trailer yard window in status idle. */
fun newDefaultTrailerYardWindow(code: String, label: String): DefaultTrailerYardWindow {
    return DefaultTrailerYardWindow(
        code = code,
        label = label,
        windowDays = 0,
        driverId = "",
        reviewer = "",
        endsAt = 0,
        contactEmail = "",
        revision = 0,
        margin = 0.0,
        source = "",
        dock = "",
        status = DefaultTrailerYardWindowStatus.IDLE,
    )
}
