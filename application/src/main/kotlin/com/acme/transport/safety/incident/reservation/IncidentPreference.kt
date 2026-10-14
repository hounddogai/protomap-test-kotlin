package com.acme.transport.safety.incident.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident preference is in its life cycle. */
enum class IncidentPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a incident preference and its current status. */
data class IncidentPreference(
    var code: String,
    var title: String,
    var windowDays: Int,
    var maximum: Double,
    var dock: String,
    var trailerId: String,
    var ratio: Double,
    var category: String,
    var reviewer: String,
    var status: IncidentPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentPreferenceStatus): Boolean {
        when (status) {
            IncidentPreferenceStatus.NEW -> {
                when (next) {
                    IncidentPreferenceStatus.RUNNING, IncidentPreferenceStatus.FAILED -> return true
                }
                return false
            }
            IncidentPreferenceStatus.QUEUED -> {
                when (next) {
                    IncidentPreferenceStatus.RUNNING, IncidentPreferenceStatus.DONE -> return true
                }
                return false
            }
            IncidentPreferenceStatus.RUNNING -> {
                when (next) {
                    IncidentPreferenceStatus.DONE, IncidentPreferenceStatus.FAILED -> return true
                }
                return false
            }
            IncidentPreferenceStatus.DONE -> return next == IncidentPreferenceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: IncidentPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("incident preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedLoad(): Double = round((ratio - windowDays.toDouble()) * 100) / 100

    fun snapshot(): IncidentPreference {
        return IncidentPreference(
            code = code,
            title = title,
            windowDays = windowDays,
            maximum = maximum,
            dock = dock,
            trailerId = trailerId,
            ratio = ratio,
            category = category,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a incident preference in status new. */
fun newIncidentPreference(code: String, title: String): IncidentPreference {
    return IncidentPreference(
        code = code,
        title = title,
        windowDays = 0,
        maximum = 0.0,
        dock = "",
        trailerId = "",
        ratio = 0.0,
        category = "",
        reviewer = "",
        status = IncidentPreferenceStatus.NEW,
    )
}
