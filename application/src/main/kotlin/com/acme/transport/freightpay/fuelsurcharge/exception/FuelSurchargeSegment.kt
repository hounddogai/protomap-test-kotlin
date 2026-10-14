package com.acme.transport.freightpay.fuelsurcharge.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge segment is in its life cycle. */
enum class FuelSurchargeSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fuel surcharge segment. Its code is unique within a tenant. */
data class FuelSurchargeSegment(
    var code: String,
    var name: String,
    var region: String,
    var checksum: String,
    var updatedAt: Int,
    var odometer: Int,
    var stopCount: Int,
    var windowDays: Int,
    var status: FuelSurchargeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeSegmentStatus): Boolean {
        when (status) {
            FuelSurchargeSegmentStatus.DRAFT -> return next == FuelSurchargeSegmentStatus.PENDING || next == FuelSurchargeSegmentStatus.CLOSED
            FuelSurchargeSegmentStatus.PENDING -> return next == FuelSurchargeSegmentStatus.ACTIVE
            FuelSurchargeSegmentStatus.ACTIVE -> {
                when (next) {
                    FuelSurchargeSegmentStatus.CLOSED -> return true
                }
                return false
            }
            FuelSurchargeSegmentStatus.SUSPENDED -> return next == FuelSurchargeSegmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge segment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netTotal(): Double = round(windowDays.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): FuelSurchargeSegment {
        return FuelSurchargeSegment(
            code = code,
            name = name,
            region = region,
            checksum = checksum,
            updatedAt = updatedAt,
            odometer = odometer,
            stopCount = stopCount,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fuel surcharge segment in status draft. */
fun newFuelSurchargeSegment(code: String, name: String): FuelSurchargeSegment {
    return FuelSurchargeSegment(
        code = code,
        name = name,
        region = "",
        checksum = "",
        updatedAt = 0,
        odometer = 0,
        stopCount = 0,
        windowDays = 0,
        status = FuelSurchargeSegmentStatus.DRAFT,
    )
}
