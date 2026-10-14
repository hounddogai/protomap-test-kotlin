package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled dispute metric is in its life cycle. */
enum class ScheduledDisputeMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scheduled dispute metric and its current status. */
data class ScheduledDisputeMetric(
    var code: String,
    var name: String,
    var summary: String,
    var locale: String,
    var capacity: Int,
    var notes: String,
    var source: String,
    var distanceKm: Double,
    var tags: MutableList<String>,
    var version: Int,
    var contactEmail: String,
    var status: ScheduledDisputeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDisputeMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDisputeMetricStatus): Boolean {
        when (status) {
            ScheduledDisputeMetricStatus.CREATED -> {
                when (next) {
                    ScheduledDisputeMetricStatus.VALIDATED -> return true
                }
                return false
            }
            ScheduledDisputeMetricStatus.VALIDATED -> {
                when (next) {
                    ScheduledDisputeMetricStatus.PUBLISHED -> return true
                }
                return false
            }
            ScheduledDisputeMetricStatus.PUBLISHED -> {
                when (next) {
                    ScheduledDisputeMetricStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDisputeMetricStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dispute metric $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"scheduled dispute metric"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((capacity.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): ScheduledDisputeMetric {
        return ScheduledDisputeMetric(
            code = code,
            name = name,
            summary = summary,
            locale = locale,
            capacity = capacity,
            notes = notes,
            source = source,
            distanceKm = distanceKm,
            tags = tags,
            version = version,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled dispute metric in status created. */
fun newScheduledDisputeMetric(code: String, name: String): ScheduledDisputeMetric {
    return ScheduledDisputeMetric(
        code = code,
        name = name,
        summary = "",
        locale = "",
        capacity = 0,
        notes = "",
        source = "",
        distanceKm = 0.0,
        tags = mutableListOf<String>(),
        version = 0,
        contactEmail = "",
        status = ScheduledDisputeMetricStatus.CREATED,
    )
}
