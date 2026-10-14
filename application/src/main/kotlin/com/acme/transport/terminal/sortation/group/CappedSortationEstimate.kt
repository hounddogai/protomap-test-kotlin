package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped sortation estimate is in its life cycle. */
enum class CappedSortationEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped sortation estimate as the sortation team keeps it. */
data class CappedSortationEstimate(
    var code: String,
    var label: String,
    var currency: String,
    var country: String,
    var capacity: Int,
    var active: Boolean,
    var stopCount: Int,
    var floorValue: Double,
    var title: String,
    var ownerId: String,
    var endsAt: Int,
    var status: CappedSortationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedSortationEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedSortationEstimateStatus): Boolean {
        when (status) {
            CappedSortationEstimateStatus.DRAFT -> {
                when (next) {
                    CappedSortationEstimateStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedSortationEstimateStatus.PENDING -> {
                when (next) {
                    CappedSortationEstimateStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedSortationEstimateStatus.ACTIVE -> return next == CappedSortationEstimateStatus.SUSPENDED || next == CappedSortationEstimateStatus.CLOSED
            CappedSortationEstimateStatus.SUSPENDED -> return next == CappedSortationEstimateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedSortationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("capped sortation estimate $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capped sortation estimate"} $code"

    fun adjustedLoad(): Double = round((stopCount.toDouble() + floorValue) * 100) / 100

    fun snapshot(): CappedSortationEstimate {
        return CappedSortationEstimate(
            code = code,
            label = label,
            currency = currency,
            country = country,
            capacity = capacity,
            active = active,
            stopCount = stopCount,
            floorValue = floorValue,
            title = title,
            ownerId = ownerId,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped sortation estimate in status draft. */
fun newCappedSortationEstimate(code: String, label: String): CappedSortationEstimate {
    return CappedSortationEstimate(
        code = code,
        label = label,
        currency = "",
        country = "",
        capacity = 0,
        active = false,
        stopCount = 0,
        floorValue = 0.0,
        title = "",
        ownerId = "",
        endsAt = 0,
        status = CappedSortationEstimateStatus.DRAFT,
    )
}
