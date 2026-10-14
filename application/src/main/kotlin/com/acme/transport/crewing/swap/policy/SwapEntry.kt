package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap entry is in its life cycle. */
enum class SwapEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A swap entry. Its code is unique within a tenant. */
data class SwapEntry(
    var code: String,
    var name: String,
    var discount: Double,
    var cost: Double,
    var checksum: String,
    var shiftCode: String,
    var fuelLiters: Double,
    var stopCount: Int,
    var trailerId: String,
    var updatedAt: Int,
    var status: SwapEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapEntryStatus): Boolean {
        when (status) {
            SwapEntryStatus.OPEN -> {
                when (next) {
                    SwapEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            SwapEntryStatus.IN_REVIEW -> return next == SwapEntryStatus.RESOLVED
            SwapEntryStatus.RESOLVED -> {
                when (next) {
                    SwapEntryStatus.REOPENED -> return true
                }
                return false
            }
            SwapEntryStatus.REOPENED -> {
                when (next) {
                    SwapEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapEntryStatus) {
        if (!canMoveTo(next)) {
            error("swap entry $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round((discount + stopCount.toDouble()) * 100) / 100

    fun snapshot(): SwapEntry {
        return SwapEntry(
            code = code,
            name = name,
            discount = discount,
            cost = cost,
            checksum = checksum,
            shiftCode = shiftCode,
            fuelLiters = fuelLiters,
            stopCount = stopCount,
            trailerId = trailerId,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a swap entry in status open. */
fun newSwapEntry(code: String, name: String): SwapEntry {
    return SwapEntry(
        code = code,
        name = name,
        discount = 0.0,
        cost = 0.0,
        checksum = "",
        shiftCode = "",
        fuelLiters = 0.0,
        stopCount = 0,
        trailerId = "",
        updatedAt = 0,
        status = SwapEntryStatus.OPEN,
    )
}
