package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive tender window records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a adaptive tender window is in its life cycle. */
enum class AdaptiveTenderWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One adaptive tender window, with what the planners need to act on it. */
data class AdaptiveTenderWindow(
    var code: String,
    var title: String,
    var odometer: Int,
    var ratio: Double,
    var summary: String,
    var budget: Double,
    var status: AdaptiveTenderWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTenderWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTenderWindowStatus): Boolean {
        when (status) {
            AdaptiveTenderWindowStatus.NEW -> {
                when (next) {
                    AdaptiveTenderWindowStatus.RUNNING -> return true
                }
                return false
            }
            AdaptiveTenderWindowStatus.QUEUED -> {
                when (next) {
                    AdaptiveTenderWindowStatus.RUNNING, AdaptiveTenderWindowStatus.DONE -> return true
                }
                return false
            }
            AdaptiveTenderWindowStatus.RUNNING -> return next == AdaptiveTenderWindowStatus.FAILED
            AdaptiveTenderWindowStatus.DONE -> {
                when (next) {
                    AdaptiveTenderWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTenderWindowStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tender window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((ratio - odometer.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveTenderWindow {
        return AdaptiveTenderWindow(
            code = code,
            title = title,
            odometer = odometer,
            ratio = ratio,
            summary = summary,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive tender window in status new. */
fun newAdaptiveTenderWindow(code: String, title: String): AdaptiveTenderWindow {
    return AdaptiveTenderWindow(
        code = code,
        title = title,
        odometer = 0,
        ratio = 0.0,
        summary = "",
        budget = 0.0,
        status = AdaptiveTenderWindowStatus.NEW,
    )
}
