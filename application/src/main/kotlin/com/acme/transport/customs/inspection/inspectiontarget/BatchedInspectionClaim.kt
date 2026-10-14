package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched inspection claim is in its life cycle. */
enum class BatchedInspectionClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a batched inspection claim and its current status. */
data class BatchedInspectionClaim(
    var code: String,
    var name: String,
    var vehicleId: String,
    var distanceKm: Double,
    var threshold: Double,
    var phone: String,
    var discount: Double,
    var archived: Boolean,
    var country: String,
    var rate: Double,
    var status: BatchedInspectionClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedInspectionClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedInspectionClaimStatus): Boolean {
        when (status) {
            BatchedInspectionClaimStatus.NEW -> {
                when (next) {
                    BatchedInspectionClaimStatus.FAILED -> return true
                }
                return false
            }
            BatchedInspectionClaimStatus.QUEUED -> return next == BatchedInspectionClaimStatus.DONE
            BatchedInspectionClaimStatus.RUNNING -> {
                when (next) {
                    BatchedInspectionClaimStatus.FAILED -> return true
                }
                return false
            }
            BatchedInspectionClaimStatus.DONE -> return next == BatchedInspectionClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BatchedInspectionClaimStatus) {
        if (!canMoveTo(next)) {
            error("batched inspection claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched inspection claim"} $code"

    fun adjustedExposure(): Double = round((discount + distanceKm) * 100) / 100

    fun snapshot(): BatchedInspectionClaim {
        return BatchedInspectionClaim(
            code = code,
            name = name,
            vehicleId = vehicleId,
            distanceKm = distanceKm,
            threshold = threshold,
            phone = phone,
            discount = discount,
            archived = archived,
            country = country,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched inspection claim in status new. */
fun newBatchedInspectionClaim(code: String, name: String): BatchedInspectionClaim {
    return BatchedInspectionClaim(
        code = code,
        name = name,
        vehicleId = "",
        distanceKm = 0.0,
        threshold = 0.0,
        phone = "",
        discount = 0.0,
        archived = false,
        country = "",
        rate = 0.0,
        status = BatchedInspectionClaimStatus.NEW,
    )
}
