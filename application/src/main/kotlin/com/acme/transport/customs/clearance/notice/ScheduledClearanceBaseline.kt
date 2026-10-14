package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled clearance baseline is in its life cycle. */
enum class ScheduledClearanceBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled clearance baseline as the clearance team keeps it. */
data class ScheduledClearanceBaseline(
    var code: String,
    var title: String,
    var odometer: Int,
    var revision: Int,
    var score: Double,
    var summary: String,
    var graceDays: Int,
    var timezone: String,
    var pallets: Int,
    var endsAt: Int,
    var status: ScheduledClearanceBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledClearanceBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledClearanceBaselineStatus): Boolean {
        when (status) {
            ScheduledClearanceBaselineStatus.DRAFT -> return next == ScheduledClearanceBaselineStatus.PENDING || next == ScheduledClearanceBaselineStatus.SUSPENDED
            ScheduledClearanceBaselineStatus.PENDING -> return next == ScheduledClearanceBaselineStatus.ACTIVE
            ScheduledClearanceBaselineStatus.ACTIVE -> {
                when (next) {
                    ScheduledClearanceBaselineStatus.SUSPENDED, ScheduledClearanceBaselineStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledClearanceBaselineStatus.SUSPENDED -> return next == ScheduledClearanceBaselineStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledClearanceBaselineStatus) {
        if (!canMoveTo(next)) {
            error("scheduled clearance baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"scheduled clearance baseline"} $code"

    fun netValue(): Double = round((score - odometer.toDouble()) * 100) / 100

    fun snapshot(): ScheduledClearanceBaseline {
        return ScheduledClearanceBaseline(
            code = code,
            title = title,
            odometer = odometer,
            revision = revision,
            score = score,
            summary = summary,
            graceDays = graceDays,
            timezone = timezone,
            pallets = pallets,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled clearance baseline in status draft. */
fun newScheduledClearanceBaseline(code: String, title: String): ScheduledClearanceBaseline {
    return ScheduledClearanceBaseline(
        code = code,
        title = title,
        odometer = 0,
        revision = 0,
        score = 0.0,
        summary = "",
        graceDays = 0,
        timezone = "",
        pallets = 0,
        endsAt = 0,
        status = ScheduledClearanceBaselineStatus.DRAFT,
    )
}
