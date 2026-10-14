package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge bundle is in its life cycle. */
enum class FuelSurchargeBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fuel surcharge bundle. Its code is unique within a tenant. */
data class FuelSurchargeBundle(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var description: String,
    var revision: Int,
    var startsAt: Int,
    var margin: Double,
    var status: FuelSurchargeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeBundleStatus): Boolean {
        when (status) {
            FuelSurchargeBundleStatus.CREATED -> return next == FuelSurchargeBundleStatus.VALIDATED
            FuelSurchargeBundleStatus.VALIDATED -> return next == FuelSurchargeBundleStatus.PUBLISHED || next == FuelSurchargeBundleStatus.RETIRED
            FuelSurchargeBundleStatus.PUBLISHED -> return next == FuelSurchargeBundleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeBundleStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge bundle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"fuel surcharge bundle"} $code"

    fun effectiveLoad(): Double = round((distanceKm + revision.toDouble()) * 100) / 100

    fun snapshot(): FuelSurchargeBundle {
        return FuelSurchargeBundle(
            code = code,
            name = name,
            distanceKm = distanceKm,
            description = description,
            revision = revision,
            startsAt = startsAt,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel surcharge bundle in status created. */
fun newFuelSurchargeBundle(code: String, name: String): FuelSurchargeBundle {
    return FuelSurchargeBundle(
        code = code,
        name = name,
        distanceKm = 0.0,
        description = "",
        revision = 0,
        startsAt = 0,
        margin = 0.0,
        status = FuelSurchargeBundleStatus.CREATED,
    )
}
