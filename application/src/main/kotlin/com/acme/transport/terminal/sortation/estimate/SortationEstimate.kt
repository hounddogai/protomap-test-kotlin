package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation estimate records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation estimate is in its life cycle. */
enum class SortationEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One sortation estimate, with what the planners need to act on it. */
data class SortationEstimate(
    var code: String,
    var name: String,
    var discount: Double,
    var loadKg: Double,
    var rate: Double,
    var routeCode: String,
    var depot: String,
    var pallets: Int,
    var status: SortationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationEstimateStatus): Boolean {
        when (status) {
            SortationEstimateStatus.CREATED -> return next == SortationEstimateStatus.VALIDATED || next == SortationEstimateStatus.RETIRED
            SortationEstimateStatus.VALIDATED -> {
                when (next) {
                    SortationEstimateStatus.RETIRED -> return true
                }
                return false
            }
            SortationEstimateStatus.PUBLISHED -> return next == SortationEstimateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SortationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("sortation estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"sortation estimate"} $code"

    fun adjustedLoad(): Double = round(loadKg * rate * 100) / 100

    fun snapshot(): SortationEstimate {
        return SortationEstimate(
            code = code,
            name = name,
            discount = discount,
            loadKg = loadKg,
            rate = rate,
            routeCode = routeCode,
            depot = depot,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a sortation estimate in status created. */
fun newSortationEstimate(code: String, name: String): SortationEstimate {
    return SortationEstimate(
        code = code,
        name = name,
        discount = 0.0,
        loadKg = 0.0,
        rate = 0.0,
        routeCode = "",
        depot = "",
        pallets = 0,
        status = SortationEstimateStatus.CREATED,
    )
}
