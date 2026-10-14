package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global fuel surcharge batch is in its life cycle. */
enum class GlobalFuelSurchargeBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One global fuel surcharge batch, with what the planners need to act on it. */
data class GlobalFuelSurchargeBatch(
    var code: String,
    var name: String,
    var routeCode: String,
    var depot: String,
    var retries: Int,
    var ratio: Double,
    var cost: Double,
    var updatedAt: Int,
    var status: GlobalFuelSurchargeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFuelSurchargeBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFuelSurchargeBatchStatus): Boolean {
        when (status) {
            GlobalFuelSurchargeBatchStatus.OPEN -> {
                when (next) {
                    GlobalFuelSurchargeBatchStatus.RESOLVED -> return true
                }
                return false
            }
            GlobalFuelSurchargeBatchStatus.IN_REVIEW -> return next == GlobalFuelSurchargeBatchStatus.RESOLVED || next == GlobalFuelSurchargeBatchStatus.REOPENED
            GlobalFuelSurchargeBatchStatus.RESOLVED -> return next == GlobalFuelSurchargeBatchStatus.ARCHIVED
            GlobalFuelSurchargeBatchStatus.REOPENED -> {
                when (next) {
                    GlobalFuelSurchargeBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalFuelSurchargeBatchStatus) {
        if (!canMoveTo(next)) {
            error("global fuel surcharge batch $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global fuel surcharge batch"} $code"

    fun weightedVolume(): Double = round((retries.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalFuelSurchargeBatch {
        return GlobalFuelSurchargeBatch(
            code = code,
            name = name,
            routeCode = routeCode,
            depot = depot,
            retries = retries,
            ratio = ratio,
            cost = cost,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global fuel surcharge batch in status open. */
fun newGlobalFuelSurchargeBatch(code: String, name: String): GlobalFuelSurchargeBatch {
    return GlobalFuelSurchargeBatch(
        code = code,
        name = name,
        routeCode = "",
        depot = "",
        retries = 0,
        ratio = 0.0,
        cost = 0.0,
        updatedAt = 0,
        status = GlobalFuelSurchargeBatchStatus.OPEN,
    )
}
