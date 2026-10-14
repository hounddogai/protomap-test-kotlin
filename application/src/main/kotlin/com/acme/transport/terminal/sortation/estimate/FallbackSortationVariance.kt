package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback sortation variance is in its life cycle. */
enum class FallbackSortationVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fallback sortation variance, with what the planners need to act on it. */
data class FallbackSortationVariance(
    var code: String,
    var title: String,
    var name: String,
    var floorValue: Double,
    var externalRef: String,
    var endsAt: Int,
    var vehicleId: String,
    var pinned: Boolean,
    var unit: String,
    var weight: Double,
    var ratio: Double,
    var status: FallbackSortationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackSortationVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackSortationVarianceStatus): Boolean {
        when (status) {
            FallbackSortationVarianceStatus.IDLE -> {
                when (next) {
                    FallbackSortationVarianceStatus.READY, FallbackSortationVarianceStatus.STOPPED -> return true
                }
                return false
            }
            FallbackSortationVarianceStatus.WARMING -> {
                when (next) {
                    FallbackSortationVarianceStatus.READY -> return true
                }
                return false
            }
            FallbackSortationVarianceStatus.READY -> return next == FallbackSortationVarianceStatus.STOPPED
            FallbackSortationVarianceStatus.DRAINING -> {
                when (next) {
                    FallbackSortationVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackSortationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback sortation variance $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round((floorValue - endsAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackSortationVariance {
        return FallbackSortationVariance(
            code = code,
            title = title,
            name = name,
            floorValue = floorValue,
            externalRef = externalRef,
            endsAt = endsAt,
            vehicleId = vehicleId,
            pinned = pinned,
            unit = unit,
            weight = weight,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback sortation variance in status idle. */
fun newFallbackSortationVariance(code: String, title: String): FallbackSortationVariance {
    return FallbackSortationVariance(
        code = code,
        title = title,
        name = "",
        floorValue = 0.0,
        externalRef = "",
        endsAt = 0,
        vehicleId = "",
        pinned = false,
        unit = "",
        weight = 0.0,
        ratio = 0.0,
        status = FallbackSortationVarianceStatus.IDLE,
    )
}
