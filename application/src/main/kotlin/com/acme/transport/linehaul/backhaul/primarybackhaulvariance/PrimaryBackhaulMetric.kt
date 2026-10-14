package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary backhaul metric is in its life cycle. */
enum class PrimaryBackhaulMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary backhaul metric as the backhaul team keeps it. */
data class PrimaryBackhaulMetric(
    var code: String,
    var label: String,
    var amount: Double,
    var limit: Int,
    var createdAt: Int,
    var revision: Int,
    var status: PrimaryBackhaulMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryBackhaulMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryBackhaulMetricStatus): Boolean {
        when (status) {
            PrimaryBackhaulMetricStatus.NEW -> return next == PrimaryBackhaulMetricStatus.QUEUED || next == PrimaryBackhaulMetricStatus.FAILED
            PrimaryBackhaulMetricStatus.QUEUED -> return next == PrimaryBackhaulMetricStatus.RUNNING || next == PrimaryBackhaulMetricStatus.DONE
            PrimaryBackhaulMetricStatus.RUNNING -> return next == PrimaryBackhaulMetricStatus.DONE
            PrimaryBackhaulMetricStatus.DONE -> return next == PrimaryBackhaulMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryBackhaulMetricStatus) {
        if (!canMoveTo(next)) {
            error("primary backhaul metric $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"primary backhaul metric"} $code"

    fun projectedVolume(): Double = round((amount + limit.toDouble()) * 100) / 100

    fun snapshot(): PrimaryBackhaulMetric {
        return PrimaryBackhaulMetric(
            code = code,
            label = label,
            amount = amount,
            limit = limit,
            createdAt = createdAt,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary backhaul metric in status new. */
fun newPrimaryBackhaulMetric(code: String, label: String): PrimaryBackhaulMetric {
    return PrimaryBackhaulMetric(
        code = code,
        label = label,
        amount = 0.0,
        limit = 0,
        createdAt = 0,
        revision = 0,
        status = PrimaryBackhaulMetricStatus.NEW,
    )
}
