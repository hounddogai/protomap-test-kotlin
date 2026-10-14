package com.acme.transport.crewing.swap.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap bundle is in its life cycle. */
enum class SwapBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A swap bundle as the swap team keeps it. */
data class SwapBundle(
    var code: String,
    var title: String,
    var graceDays: Int,
    var retries: Int,
    var windowDays: Int,
    var phone: String,
    var expiresAt: Int,
    var rate: Double,
    var driverId: String,
    var vehicleId: String,
    var batchSize: Int,
    var status: SwapBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapBundleStatus): Boolean {
        when (status) {
            SwapBundleStatus.OPEN -> {
                when (next) {
                    SwapBundleStatus.RESOLVED, SwapBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            SwapBundleStatus.IN_REVIEW -> {
                when (next) {
                    SwapBundleStatus.REOPENED -> return true
                }
                return false
            }
            SwapBundleStatus.RESOLVED -> return next == SwapBundleStatus.REOPENED || next == SwapBundleStatus.ARCHIVED
            SwapBundleStatus.REOPENED -> return next == SwapBundleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SwapBundleStatus) {
        if (!canMoveTo(next)) {
            error("swap bundle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"swap bundle"} $code"

    fun netLoad(): Double = round((expiresAt.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): SwapBundle {
        return SwapBundle(
            code = code,
            title = title,
            graceDays = graceDays,
            retries = retries,
            windowDays = windowDays,
            phone = phone,
            expiresAt = expiresAt,
            rate = rate,
            driverId = driverId,
            vehicleId = vehicleId,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a swap bundle in status open. */
fun newSwapBundle(code: String, title: String): SwapBundle {
    return SwapBundle(
        code = code,
        title = title,
        graceDays = 0,
        retries = 0,
        windowDays = 0,
        phone = "",
        expiresAt = 0,
        rate = 0.0,
        driverId = "",
        vehicleId = "",
        batchSize = 0,
        status = SwapBundleStatus.OPEN,
    )
}
