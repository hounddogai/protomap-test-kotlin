package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched service plan reservation is in its life cycle. */
enum class BatchedServicePlanReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One batched service plan reservation, with what the planners need to act on it. */
data class BatchedServicePlanReservation(
    var code: String,
    var title: String,
    var vehicleId: String,
    var quantity: Int,
    var currency: String,
    var threshold: Double,
    var reviewer: String,
    var lane: String,
    var status: BatchedServicePlanReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedServicePlanReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedServicePlanReservationStatus): Boolean {
        when (status) {
            BatchedServicePlanReservationStatus.PLANNED -> return next == BatchedServicePlanReservationStatus.COMPLETED || next == BatchedServicePlanReservationStatus.CANCELLED
            BatchedServicePlanReservationStatus.SCHEDULED -> return next == BatchedServicePlanReservationStatus.CANCELLED
            BatchedServicePlanReservationStatus.IN_PROGRESS -> return next == BatchedServicePlanReservationStatus.CANCELLED
            BatchedServicePlanReservationStatus.COMPLETED -> {
                when (next) {
                    BatchedServicePlanReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedServicePlanReservationStatus) {
        if (!canMoveTo(next)) {
            error("batched service plan reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round(threshold * quantity.toDouble() * 100) / 100

    fun snapshot(): BatchedServicePlanReservation {
        return BatchedServicePlanReservation(
            code = code,
            title = title,
            vehicleId = vehicleId,
            quantity = quantity,
            currency = currency,
            threshold = threshold,
            reviewer = reviewer,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched service plan reservation in status planned. */
fun newBatchedServicePlanReservation(code: String, title: String): BatchedServicePlanReservation {
    return BatchedServicePlanReservation(
        code = code,
        title = title,
        vehicleId = "",
        quantity = 0,
        currency = "",
        threshold = 0.0,
        reviewer = "",
        lane = "",
        status = BatchedServicePlanReservationStatus.PLANNED,
    )
}
