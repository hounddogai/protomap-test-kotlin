package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.text.normalize

/** Where a batched parcel forecast is in its life cycle. */
enum class BatchedParcelForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A batched parcel forecast. Its code is unique within a tenant. */
data class BatchedParcelForecast(
    var code: String,
    var title: String,
    var checksum: String,
    var carrier: String,
    var contactEmail: String,
    var vehicleId: String,
    var description: String,
    var amount: Double,
    var status: BatchedParcelForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedParcelForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedParcelForecastStatus): Boolean {
        when (status) {
            BatchedParcelForecastStatus.PLANNED -> return next == BatchedParcelForecastStatus.SCHEDULED || next == BatchedParcelForecastStatus.COMPLETED
            BatchedParcelForecastStatus.SCHEDULED -> {
                when (next) {
                    BatchedParcelForecastStatus.COMPLETED, BatchedParcelForecastStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedParcelForecastStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedParcelForecastStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedParcelForecastStatus.COMPLETED -> return next == BatchedParcelForecastStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedParcelForecastStatus) {
        if (!canMoveTo(next)) {
            error("batched parcel forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = amount

    fun snapshot(): BatchedParcelForecast {
        return BatchedParcelForecast(
            code = code,
            title = title,
            checksum = checksum,
            carrier = carrier,
            contactEmail = contactEmail,
            vehicleId = vehicleId,
            description = description,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched parcel forecast in status planned. */
fun newBatchedParcelForecast(code: String, title: String): BatchedParcelForecast {
    return BatchedParcelForecast(
        code = code,
        title = title,
        checksum = "",
        carrier = "",
        contactEmail = "",
        vehicleId = "",
        description = "",
        amount = 0.0,
        status = BatchedParcelForecastStatus.PLANNED,
    )
}
