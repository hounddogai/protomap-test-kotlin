package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered qualification metric records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a tiered qualification metric is in its life cycle. */
enum class TieredQualificationMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered qualification metric. Its code is unique within a tenant. */
data class TieredQualificationMetric(
    var code: String,
    var title: String,
    var margin: Double,
    var source: String,
    var attributes: MutableMap<String, String>,
    var tags: MutableList<String>,
    var windowDays: Int,
    var threshold: Double,
    var channel: String,
    var fuelLiters: Double,
    var status: TieredQualificationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredQualificationMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredQualificationMetricStatus): Boolean {
        when (status) {
            TieredQualificationMetricStatus.OPEN -> {
                when (next) {
                    TieredQualificationMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredQualificationMetricStatus.IN_REVIEW -> {
                when (next) {
                    TieredQualificationMetricStatus.RESOLVED, TieredQualificationMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredQualificationMetricStatus.RESOLVED -> return next == TieredQualificationMetricStatus.ARCHIVED
            TieredQualificationMetricStatus.REOPENED -> {
                when (next) {
                    TieredQualificationMetricStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredQualificationMetricStatus) {
        if (!canMoveTo(next)) {
            error("tiered qualification metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered qualification metric"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round((threshold - windowDays.toDouble()) * 100) / 100

    fun snapshot(): TieredQualificationMetric {
        return TieredQualificationMetric(
            code = code,
            title = title,
            margin = margin,
            source = source,
            attributes = attributes,
            tags = tags,
            windowDays = windowDays,
            threshold = threshold,
            channel = channel,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered qualification metric in status open. */
fun newTieredQualificationMetric(code: String, title: String): TieredQualificationMetric {
    return TieredQualificationMetric(
        code = code,
        title = title,
        margin = 0.0,
        source = "",
        attributes = mutableMapOf<String, String>(),
        tags = mutableListOf<String>(),
        windowDays = 0,
        threshold = 0.0,
        channel = "",
        fuelLiters = 0.0,
        status = TieredQualificationMetricStatus.OPEN,
    )
}
