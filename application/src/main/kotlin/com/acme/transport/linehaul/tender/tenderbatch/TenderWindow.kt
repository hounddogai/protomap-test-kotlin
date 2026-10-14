package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender window is in its life cycle. */
enum class TenderWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tender window and its current status. */
data class TenderWindow(
    var code: String,
    var name: String,
    var budget: Double,
    var trailerId: String,
    var timezone: String,
    var driverId: String,
    var attempts: Int,
    var dock: String,
    var externalRef: String,
    var channel: String,
    var locale: String,
    var status: TenderWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderWindowStatus): Boolean {
        when (status) {
            TenderWindowStatus.OPEN -> return next == TenderWindowStatus.IN_REVIEW
            TenderWindowStatus.IN_REVIEW -> return next == TenderWindowStatus.REOPENED || next == TenderWindowStatus.ARCHIVED
            TenderWindowStatus.RESOLVED -> return next == TenderWindowStatus.REOPENED || next == TenderWindowStatus.ARCHIVED
            TenderWindowStatus.REOPENED -> return next == TenderWindowStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TenderWindowStatus) {
        if (!canMoveTo(next)) {
            error("tender window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netTotal(): Double = round((attempts.toDouble() + budget) * 100) / 100

    fun snapshot(): TenderWindow {
        return TenderWindow(
            code = code,
            name = name,
            budget = budget,
            trailerId = trailerId,
            timezone = timezone,
            driverId = driverId,
            attempts = attempts,
            dock = dock,
            externalRef = externalRef,
            channel = channel,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tender window in status open. */
fun newTenderWindow(code: String, name: String): TenderWindow {
    return TenderWindow(
        code = code,
        name = name,
        budget = 0.0,
        trailerId = "",
        timezone = "",
        driverId = "",
        attempts = 0,
        dock = "",
        externalRef = "",
        channel = "",
        locale = "",
        status = TenderWindowStatus.OPEN,
    )
}
