package com.acme.transport.freightpay.audit.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted audit window is in its life cycle. */
enum class WeightedAuditWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a weighted audit window and its current status. */
data class WeightedAuditWindow(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var threshold: Double,
    var ownerId: String,
    var version: Int,
    var windowDays: Int,
    var margin: Double,
    var driverId: String,
    var status: WeightedAuditWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAuditWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAuditWindowStatus): Boolean {
        when (status) {
            WeightedAuditWindowStatus.NEW -> return next == WeightedAuditWindowStatus.QUEUED || next == WeightedAuditWindowStatus.RUNNING
            WeightedAuditWindowStatus.QUEUED -> return next == WeightedAuditWindowStatus.RUNNING || next == WeightedAuditWindowStatus.FAILED
            WeightedAuditWindowStatus.RUNNING -> return next == WeightedAuditWindowStatus.DONE || next == WeightedAuditWindowStatus.FAILED
            WeightedAuditWindowStatus.DONE -> {
                when (next) {
                    WeightedAuditWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedAuditWindowStatus) {
        if (!canMoveTo(next)) {
            error("weighted audit window $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"weighted audit window"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round((margin - windowDays.toDouble()) * 100) / 100

    fun snapshot(): WeightedAuditWindow {
        return WeightedAuditWindow(
            code = code,
            label = label,
            tags = tags,
            threshold = threshold,
            ownerId = ownerId,
            version = version,
            windowDays = windowDays,
            margin = margin,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted audit window in status new. */
fun newWeightedAuditWindow(code: String, label: String): WeightedAuditWindow {
    return WeightedAuditWindow(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        threshold = 0.0,
        ownerId = "",
        version = 0,
        windowDays = 0,
        margin = 0.0,
        driverId = "",
        status = WeightedAuditWindowStatus.NEW,
    )
}
