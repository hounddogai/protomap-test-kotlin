package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled camera event estimate is in its life cycle. */
enum class ScheduledCameraEventEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled camera event estimate. Its code is unique within a tenant. */
data class ScheduledCameraEventEstimate(
    var code: String,
    var name: String,
    var rate: Double,
    var threshold: Double,
    var pinned: Boolean,
    var summary: String,
    var quantity: Int,
    var unit: String,
    var odometer: Int,
    var status: ScheduledCameraEventEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCameraEventEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCameraEventEstimateStatus): Boolean {
        when (status) {
            ScheduledCameraEventEstimateStatus.CREATED -> {
                when (next) {
                    ScheduledCameraEventEstimateStatus.VALIDATED -> return true
                }
                return false
            }
            ScheduledCameraEventEstimateStatus.VALIDATED -> {
                when (next) {
                    ScheduledCameraEventEstimateStatus.PUBLISHED, ScheduledCameraEventEstimateStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledCameraEventEstimateStatus.PUBLISHED -> {
                when (next) {
                    ScheduledCameraEventEstimateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCameraEventEstimateStatus) {
        if (!canMoveTo(next)) {
            error("scheduled camera event estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round((rate - quantity.toDouble()) * 100) / 100

    fun snapshot(): ScheduledCameraEventEstimate {
        return ScheduledCameraEventEstimate(
            code = code,
            name = name,
            rate = rate,
            threshold = threshold,
            pinned = pinned,
            summary = summary,
            quantity = quantity,
            unit = unit,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled camera event estimate in status created. */
fun newScheduledCameraEventEstimate(code: String, name: String): ScheduledCameraEventEstimate {
    return ScheduledCameraEventEstimate(
        code = code,
        name = name,
        rate = 0.0,
        threshold = 0.0,
        pinned = false,
        summary = "",
        quantity = 0,
        unit = "",
        odometer = 0,
        status = ScheduledCameraEventEstimateStatus.CREATED,
    )
}
