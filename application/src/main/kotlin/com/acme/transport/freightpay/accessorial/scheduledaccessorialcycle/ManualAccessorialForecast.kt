package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual accessorial forecast is in its life cycle. */
enum class ManualAccessorialForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A manual accessorial forecast as the accessorial team keeps it. */
data class ManualAccessorialForecast(
    var code: String,
    var label: String,
    var endsAt: Int,
    var tags: MutableList<String>,
    var ratio: Double,
    var windowDays: Int,
    var budget: Double,
    var notes: String,
    var name: String,
    var status: ManualAccessorialForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAccessorialForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAccessorialForecastStatus): Boolean {
        when (status) {
            ManualAccessorialForecastStatus.OPEN -> {
                when (next) {
                    ManualAccessorialForecastStatus.IN_REVIEW -> return true
                }
                return false
            }
            ManualAccessorialForecastStatus.IN_REVIEW -> return next == ManualAccessorialForecastStatus.REOPENED
            ManualAccessorialForecastStatus.RESOLVED -> {
                when (next) {
                    ManualAccessorialForecastStatus.REOPENED, ManualAccessorialForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualAccessorialForecastStatus.REOPENED -> return next == ManualAccessorialForecastStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ManualAccessorialForecastStatus) {
        if (!canMoveTo(next)) {
            error("manual accessorial forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((ratio - windowDays.toDouble()) * 100) / 100

    fun snapshot(): ManualAccessorialForecast {
        return ManualAccessorialForecast(
            code = code,
            label = label,
            endsAt = endsAt,
            tags = tags,
            ratio = ratio,
            windowDays = windowDays,
            budget = budget,
            notes = notes,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual accessorial forecast in status open. */
fun newManualAccessorialForecast(code: String, label: String): ManualAccessorialForecast {
    return ManualAccessorialForecast(
        code = code,
        label = label,
        endsAt = 0,
        tags = mutableListOf<String>(),
        ratio = 0.0,
        windowDays = 0,
        budget = 0.0,
        notes = "",
        name = "",
        status = ManualAccessorialForecastStatus.OPEN,
    )
}
