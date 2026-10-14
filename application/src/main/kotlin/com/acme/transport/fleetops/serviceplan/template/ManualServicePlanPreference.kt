package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual service plan preference is in its life cycle. */
enum class ManualServicePlanPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One manual service plan preference, with what the planners need to act on it. */
data class ManualServicePlanPreference(
    var code: String,
    var title: String,
    var startsAt: Int,
    var margin: Double,
    var unit: String,
    var channel: String,
    var currency: String,
    var updatedAt: Int,
    var dock: String,
    var visible: Boolean,
    var threshold: Double,
    var status: ManualServicePlanPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualServicePlanPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualServicePlanPreferenceStatus): Boolean {
        when (status) {
            ManualServicePlanPreferenceStatus.IDLE -> return next == ManualServicePlanPreferenceStatus.STOPPED
            ManualServicePlanPreferenceStatus.WARMING -> {
                when (next) {
                    ManualServicePlanPreferenceStatus.DRAINING -> return true
                }
                return false
            }
            ManualServicePlanPreferenceStatus.READY -> return next == ManualServicePlanPreferenceStatus.STOPPED
            ManualServicePlanPreferenceStatus.DRAINING -> return next == ManualServicePlanPreferenceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualServicePlanPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("manual service plan preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((updatedAt.toDouble() + margin) * 100) / 100

    fun snapshot(): ManualServicePlanPreference {
        return ManualServicePlanPreference(
            code = code,
            title = title,
            startsAt = startsAt,
            margin = margin,
            unit = unit,
            channel = channel,
            currency = currency,
            updatedAt = updatedAt,
            dock = dock,
            visible = visible,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a manual service plan preference in status idle. */
fun newManualServicePlanPreference(code: String, title: String): ManualServicePlanPreference {
    return ManualServicePlanPreference(
        code = code,
        title = title,
        startsAt = 0,
        margin = 0.0,
        unit = "",
        channel = "",
        currency = "",
        updatedAt = 0,
        dock = "",
        visible = false,
        threshold = 0.0,
        status = ManualServicePlanPreferenceStatus.IDLE,
    )
}
