package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient service area request is in its life cycle. */
enum class LenientServiceAreaRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient service area request. Its code is unique within a tenant. */
data class LenientServiceAreaRequest(
    var code: String,
    var label: String,
    var timezone: String,
    var pallets: Int,
    var updatedAt: Int,
    var unit: String,
    var fuelLiters: Double,
    var minimum: Double,
    var quantity: Int,
    var status: LenientServiceAreaRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientServiceAreaRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientServiceAreaRequestStatus): Boolean {
        when (status) {
            LenientServiceAreaRequestStatus.PROPOSED -> return next == LenientServiceAreaRequestStatus.REJECTED || next == LenientServiceAreaRequestStatus.WITHDRAWN
            LenientServiceAreaRequestStatus.APPROVED -> return next == LenientServiceAreaRequestStatus.REJECTED || next == LenientServiceAreaRequestStatus.WITHDRAWN
            LenientServiceAreaRequestStatus.REJECTED -> return next == LenientServiceAreaRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientServiceAreaRequestStatus) {
        if (!canMoveTo(next)) {
            error("lenient service area request $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round((fuelLiters - pallets.toDouble()) * 100) / 100

    fun snapshot(): LenientServiceAreaRequest {
        return LenientServiceAreaRequest(
            code = code,
            label = label,
            timezone = timezone,
            pallets = pallets,
            updatedAt = updatedAt,
            unit = unit,
            fuelLiters = fuelLiters,
            minimum = minimum,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient service area request in status proposed. */
fun newLenientServiceAreaRequest(code: String, label: String): LenientServiceAreaRequest {
    return LenientServiceAreaRequest(
        code = code,
        label = label,
        timezone = "",
        pallets = 0,
        updatedAt = 0,
        unit = "",
        fuelLiters = 0.0,
        minimum = 0.0,
        quantity = 0,
        status = LenientServiceAreaRequestStatus.PROPOSED,
    )
}
