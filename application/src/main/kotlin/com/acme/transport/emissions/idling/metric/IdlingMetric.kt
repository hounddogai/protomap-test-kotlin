package com.acme.transport.emissions.idling.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The idling metric records of emissions's idling work, and the rules, stores, and calculations around them.

/** Where a idling metric is in its life cycle. */
enum class IdlingMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A idling metric as the idling team keeps it. */
data class IdlingMetric(
    var code: String,
    var title: String,
    var ownerId: String,
    var score: Double,
    var dock: String,
    var category: String,
    var routeCode: String,
    var notes: String,
    var shiftCode: String,
    var region: String,
    var sequence: Int,
    var status: IdlingMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingMetricStatus): Boolean {
        when (status) {
            IdlingMetricStatus.NEW -> return next == IdlingMetricStatus.QUEUED
            IdlingMetricStatus.QUEUED -> {
                when (next) {
                    IdlingMetricStatus.DONE -> return true
                }
                return false
            }
            IdlingMetricStatus.RUNNING -> return next == IdlingMetricStatus.FAILED
            IdlingMetricStatus.DONE -> {
                when (next) {
                    IdlingMetricStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingMetricStatus) {
        if (!canMoveTo(next)) {
            error("idling metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round((score - sequence.toDouble()) * 100) / 100

    fun snapshot(): IdlingMetric {
        return IdlingMetric(
            code = code,
            title = title,
            ownerId = ownerId,
            score = score,
            dock = dock,
            category = category,
            routeCode = routeCode,
            notes = notes,
            shiftCode = shiftCode,
            region = region,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a idling metric in status new. */
fun newIdlingMetric(code: String, title: String): IdlingMetric {
    return IdlingMetric(
        code = code,
        title = title,
        ownerId = "",
        score = 0.0,
        dock = "",
        category = "",
        routeCode = "",
        notes = "",
        shiftCode = "",
        region = "",
        sequence = 0,
        status = IdlingMetricStatus.NEW,
    )
}
