package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback capacity bundle is in its life cycle. */
enum class FallbackCapacityBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback capacity bundle. Its code is unique within a tenant. */
data class FallbackCapacityBundle(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var source: String,
    var threshold: Double,
    var etaMinutes: Int,
    var ratio: Double,
    var odometer: Int,
    var status: FallbackCapacityBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCapacityBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCapacityBundleStatus): Boolean {
        when (status) {
            FallbackCapacityBundleStatus.DRAFT -> {
                when (next) {
                    FallbackCapacityBundleStatus.PENDING, FallbackCapacityBundleStatus.ACTIVE -> return true
                }
                return false
            }
            FallbackCapacityBundleStatus.PENDING -> {
                when (next) {
                    FallbackCapacityBundleStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackCapacityBundleStatus.ACTIVE -> return next == FallbackCapacityBundleStatus.SUSPENDED
            FallbackCapacityBundleStatus.SUSPENDED -> return next == FallbackCapacityBundleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackCapacityBundleStatus) {
        if (!canMoveTo(next)) {
            error("fallback capacity bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveTotal(): Double = round((ratio + odometer.toDouble()) * 100) / 100

    fun snapshot(): FallbackCapacityBundle {
        return FallbackCapacityBundle(
            code = code,
            title = title,
            pinned = pinned,
            source = source,
            threshold = threshold,
            etaMinutes = etaMinutes,
            ratio = ratio,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fallback capacity bundle in status draft. */
fun newFallbackCapacityBundle(code: String, title: String): FallbackCapacityBundle {
    return FallbackCapacityBundle(
        code = code,
        title = title,
        pinned = false,
        source = "",
        threshold = 0.0,
        etaMinutes = 0,
        ratio = 0.0,
        odometer = 0,
        status = FallbackCapacityBundleStatus.DRAFT,
    )
}
