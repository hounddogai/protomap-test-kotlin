package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.text.normalize

/** Where a regional sortation adjustment is in its life cycle. */
enum class RegionalSortationAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One regional sortation adjustment, with what the planners need to act on it. */
data class RegionalSortationAdjustment(
    var code: String,
    var title: String,
    var routeCode: String,
    var summary: String,
    var carrier: String,
    var stopCount: Int,
    var currency: String,
    var source: String,
    var name: String,
    var phone: String,
    var notes: String,
    var status: RegionalSortationAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalSortationAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalSortationAdjustmentStatus): Boolean {
        when (status) {
            RegionalSortationAdjustmentStatus.PROPOSED -> return next == RegionalSortationAdjustmentStatus.APPROVED || next == RegionalSortationAdjustmentStatus.WITHDRAWN
            RegionalSortationAdjustmentStatus.APPROVED -> {
                when (next) {
                    RegionalSortationAdjustmentStatus.REJECTED, RegionalSortationAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalSortationAdjustmentStatus.REJECTED -> return next == RegionalSortationAdjustmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalSortationAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("regional sortation adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional sortation adjustment"} $code"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): RegionalSortationAdjustment {
        return RegionalSortationAdjustment(
            code = code,
            title = title,
            routeCode = routeCode,
            summary = summary,
            carrier = carrier,
            stopCount = stopCount,
            currency = currency,
            source = source,
            name = name,
            phone = phone,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional sortation adjustment in status proposed. */
fun newRegionalSortationAdjustment(code: String, title: String): RegionalSortationAdjustment {
    return RegionalSortationAdjustment(
        code = code,
        title = title,
        routeCode = "",
        summary = "",
        carrier = "",
        stopCount = 0,
        currency = "",
        source = "",
        name = "",
        phone = "",
        notes = "",
        status = RegionalSortationAdjustmentStatus.PROPOSED,
    )
}
