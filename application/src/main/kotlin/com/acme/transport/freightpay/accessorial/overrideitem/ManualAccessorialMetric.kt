package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual accessorial metric is in its life cycle. */
enum class ManualAccessorialMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual accessorial metric as the accessorial team keeps it. */
data class ManualAccessorialMetric(
    var code: String,
    var label: String,
    var threshold: Double,
    var locale: String,
    var minimum: Double,
    var retries: Int,
    var limit: Int,
    var revision: Int,
    var archived: Boolean,
    var category: String,
    var phone: String,
    var status: ManualAccessorialMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAccessorialMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAccessorialMetricStatus): Boolean {
        when (status) {
            ManualAccessorialMetricStatus.IDLE -> return next == ManualAccessorialMetricStatus.WARMING || next == ManualAccessorialMetricStatus.READY
            ManualAccessorialMetricStatus.WARMING -> {
                when (next) {
                    ManualAccessorialMetricStatus.READY -> return true
                }
                return false
            }
            ManualAccessorialMetricStatus.READY -> {
                when (next) {
                    ManualAccessorialMetricStatus.DRAINING, ManualAccessorialMetricStatus.STOPPED -> return true
                }
                return false
            }
            ManualAccessorialMetricStatus.DRAINING -> return next == ManualAccessorialMetricStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualAccessorialMetricStatus) {
        if (!canMoveTo(next)) {
            error("manual accessorial metric $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round(minimum * retries.toDouble() * 100) / 100

    fun snapshot(): ManualAccessorialMetric {
        return ManualAccessorialMetric(
            code = code,
            label = label,
            threshold = threshold,
            locale = locale,
            minimum = minimum,
            retries = retries,
            limit = limit,
            revision = revision,
            archived = archived,
            category = category,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual accessorial metric in status idle. */
fun newManualAccessorialMetric(code: String, label: String): ManualAccessorialMetric {
    return ManualAccessorialMetric(
        code = code,
        label = label,
        threshold = 0.0,
        locale = "",
        minimum = 0.0,
        retries = 0,
        limit = 0,
        revision = 0,
        archived = false,
        category = "",
        phone = "",
        status = ManualAccessorialMetricStatus.IDLE,
    )
}
