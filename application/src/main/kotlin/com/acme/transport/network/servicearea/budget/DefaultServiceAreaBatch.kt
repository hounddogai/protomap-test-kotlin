package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default service area batch is in its life cycle. */
enum class DefaultServiceAreaBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default service area batch. Its code is unique within a tenant. */
data class DefaultServiceAreaBatch(
    var code: String,
    var title: String,
    var name: String,
    var distanceKm: Double,
    var externalRef: String,
    var score: Double,
    var discount: Double,
    var version: Int,
    var pallets: Int,
    var vehicleId: String,
    var status: DefaultServiceAreaBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultServiceAreaBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultServiceAreaBatchStatus): Boolean {
        when (status) {
            DefaultServiceAreaBatchStatus.PROPOSED -> {
                when (next) {
                    DefaultServiceAreaBatchStatus.REJECTED, DefaultServiceAreaBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultServiceAreaBatchStatus.APPROVED -> return next == DefaultServiceAreaBatchStatus.WITHDRAWN
            DefaultServiceAreaBatchStatus.REJECTED -> {
                when (next) {
                    DefaultServiceAreaBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultServiceAreaBatchStatus) {
        if (!canMoveTo(next)) {
            error("default service area batch $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"default service area batch"} $code"

    fun projectedTotal(): Double = round(discount * pallets.toDouble() * 100) / 100

    fun snapshot(): DefaultServiceAreaBatch {
        return DefaultServiceAreaBatch(
            code = code,
            title = title,
            name = name,
            distanceKm = distanceKm,
            externalRef = externalRef,
            score = score,
            discount = discount,
            version = version,
            pallets = pallets,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default service area batch in status proposed. */
fun newDefaultServiceAreaBatch(code: String, title: String): DefaultServiceAreaBatch {
    return DefaultServiceAreaBatch(
        code = code,
        title = title,
        name = "",
        distanceKm = 0.0,
        externalRef = "",
        score = 0.0,
        discount = 0.0,
        version = 0,
        pallets = 0,
        vehicleId = "",
        status = DefaultServiceAreaBatchStatus.PROPOSED,
    )
}
