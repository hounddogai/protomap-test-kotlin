package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul exception is in its life cycle. */
enum class BackhaulExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A backhaul exception. Its code is unique within a tenant. */
data class BackhaulException(
    var code: String,
    var label: String,
    var startsAt: Int,
    var margin: Double,
    var graceDays: Int,
    var active: Boolean,
    var version: Int,
    var category: String,
    var durationMinutes: Int,
    var lane: String,
    var status: BackhaulExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulExceptionStatus): Boolean {
        when (status) {
            BackhaulExceptionStatus.NEW -> {
                when (next) {
                    BackhaulExceptionStatus.DONE, BackhaulExceptionStatus.FAILED -> return true
                }
                return false
            }
            BackhaulExceptionStatus.QUEUED -> return next == BackhaulExceptionStatus.RUNNING || next == BackhaulExceptionStatus.FAILED
            BackhaulExceptionStatus.RUNNING -> {
                when (next) {
                    BackhaulExceptionStatus.DONE -> return true
                }
                return false
            }
            BackhaulExceptionStatus.DONE -> {
                when (next) {
                    BackhaulExceptionStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulExceptionStatus) {
        if (!canMoveTo(next)) {
            error("backhaul exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round((durationMinutes.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): BackhaulException {
        return BackhaulException(
            code = code,
            label = label,
            startsAt = startsAt,
            margin = margin,
            graceDays = graceDays,
            active = active,
            version = version,
            category = category,
            durationMinutes = durationMinutes,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a backhaul exception in status new. */
fun newBackhaulException(code: String, label: String): BackhaulException {
    return BackhaulException(
        code = code,
        label = label,
        startsAt = 0,
        margin = 0.0,
        graceDays = 0,
        active = false,
        version = 0,
        category = "",
        durationMinutes = 0,
        lane = "",
        status = BackhaulExceptionStatus.NEW,
    )
}
