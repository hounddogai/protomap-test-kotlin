package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit metric is in its life cycle. */
enum class AuditMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A audit metric. Its code is unique within a tenant. */
data class AuditMetric(
    var code: String,
    var title: String,
    var capacity: Int,
    var startsAt: Int,
    var sequence: Int,
    var driverId: String,
    var shiftCode: String,
    var createdAt: Int,
    var minimum: Double,
    var attempts: Int,
    var amount: Double,
    var status: AuditMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditMetricStatus): Boolean {
        when (status) {
            AuditMetricStatus.CREATED -> {
                when (next) {
                    AuditMetricStatus.VALIDATED -> return true
                }
                return false
            }
            AuditMetricStatus.VALIDATED -> return next == AuditMetricStatus.PUBLISHED
            AuditMetricStatus.PUBLISHED -> return next == AuditMetricStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AuditMetricStatus) {
        if (!canMoveTo(next)) {
            error("audit metric $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"audit metric"} $code"

    fun weightedTotal(): Double = round((minimum - attempts.toDouble()) * 100) / 100

    fun snapshot(): AuditMetric {
        return AuditMetric(
            code = code,
            title = title,
            capacity = capacity,
            startsAt = startsAt,
            sequence = sequence,
            driverId = driverId,
            shiftCode = shiftCode,
            createdAt = createdAt,
            minimum = minimum,
            attempts = attempts,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a audit metric in status created. */
fun newAuditMetric(code: String, title: String): AuditMetric {
    return AuditMetric(
        code = code,
        title = title,
        capacity = 0,
        startsAt = 0,
        sequence = 0,
        driverId = "",
        shiftCode = "",
        createdAt = 0,
        minimum = 0.0,
        attempts = 0,
        amount = 0.0,
        status = AuditMetricStatus.CREATED,
    )
}
