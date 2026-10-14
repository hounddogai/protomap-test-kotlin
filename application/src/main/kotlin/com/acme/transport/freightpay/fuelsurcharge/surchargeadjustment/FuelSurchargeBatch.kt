package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge batch is in its life cycle. */
enum class FuelSurchargeBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fuel surcharge batch as the fuel surcharge team keeps it. */
data class FuelSurchargeBatch(
    var code: String,
    var name: String,
    var routeCode: String,
    var ratio: Double,
    var batchSize: Int,
    var archived: Boolean,
    var driverId: String,
    var reviewer: String,
    var status: FuelSurchargeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeBatchStatus): Boolean {
        when (status) {
            FuelSurchargeBatchStatus.OPEN -> return next == FuelSurchargeBatchStatus.ARCHIVED
            FuelSurchargeBatchStatus.IN_REVIEW -> return next == FuelSurchargeBatchStatus.RESOLVED
            FuelSurchargeBatchStatus.RESOLVED -> {
                when (next) {
                    FuelSurchargeBatchStatus.REOPENED, FuelSurchargeBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelSurchargeBatchStatus.REOPENED -> return next == FuelSurchargeBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeBatchStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((batchSize.toDouble() + ratio) * 100) / 100

    fun snapshot(): FuelSurchargeBatch {
        return FuelSurchargeBatch(
            code = code,
            name = name,
            routeCode = routeCode,
            ratio = ratio,
            batchSize = batchSize,
            archived = archived,
            driverId = driverId,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel surcharge batch in status open. */
fun newFuelSurchargeBatch(code: String, name: String): FuelSurchargeBatch {
    return FuelSurchargeBatch(
        code = code,
        name = name,
        routeCode = "",
        ratio = 0.0,
        batchSize = 0,
        archived = false,
        driverId = "",
        reviewer = "",
        status = FuelSurchargeBatchStatus.OPEN,
    )
}
