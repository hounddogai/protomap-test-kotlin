package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary dispute metric is in its life cycle. */
enum class PrimaryDisputeMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a primary dispute metric and its current status. */
data class PrimaryDisputeMetric(
    var code: String,
    var label: String,
    var source: String,
    var durationMinutes: Int,
    var limit: Int,
    var retries: Int,
    var stopCount: Int,
    var discount: Double,
    var title: String,
    var loadKg: Double,
    var etaMinutes: Int,
    var status: PrimaryDisputeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDisputeMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDisputeMetricStatus): Boolean {
        when (status) {
            PrimaryDisputeMetricStatus.CREATED -> {
                when (next) {
                    PrimaryDisputeMetricStatus.VALIDATED, PrimaryDisputeMetricStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryDisputeMetricStatus.VALIDATED -> {
                when (next) {
                    PrimaryDisputeMetricStatus.PUBLISHED, PrimaryDisputeMetricStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryDisputeMetricStatus.PUBLISHED -> {
                when (next) {
                    PrimaryDisputeMetricStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDisputeMetricStatus) {
        if (!canMoveTo(next)) {
            error("primary dispute metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveVolume(): Double = round((stopCount.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): PrimaryDisputeMetric {
        return PrimaryDisputeMetric(
            code = code,
            label = label,
            source = source,
            durationMinutes = durationMinutes,
            limit = limit,
            retries = retries,
            stopCount = stopCount,
            discount = discount,
            title = title,
            loadKg = loadKg,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary dispute metric in status created. */
fun newPrimaryDisputeMetric(code: String, label: String): PrimaryDisputeMetric {
    return PrimaryDisputeMetric(
        code = code,
        label = label,
        source = "",
        durationMinutes = 0,
        limit = 0,
        retries = 0,
        stopCount = 0,
        discount = 0.0,
        title = "",
        loadKg = 0.0,
        etaMinutes = 0,
        status = PrimaryDisputeMetricStatus.CREATED,
    )
}
