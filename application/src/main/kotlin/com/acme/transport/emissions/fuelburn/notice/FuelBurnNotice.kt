package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.kit.text.normalize

// The fuel burn notice records of emissions's fuel burn work, and the rules, stores, and calculations around
// them.

/** Where a fuel burn notice is in its life cycle. */
enum class FuelBurnNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fuel burn notice, with what the planners need to act on it. */
data class FuelBurnNotice(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var shiftCode: String,
    var stopCount: Int,
    var locale: String,
    var status: FuelBurnNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnNoticeStatus): Boolean {
        when (status) {
            FuelBurnNoticeStatus.PROPOSED -> return next == FuelBurnNoticeStatus.APPROVED
            FuelBurnNoticeStatus.APPROVED -> return next == FuelBurnNoticeStatus.REJECTED || next == FuelBurnNoticeStatus.WITHDRAWN
            FuelBurnNoticeStatus.REJECTED -> return next == FuelBurnNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FuelBurnNoticeStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fuel burn notice"} $code"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): FuelBurnNotice {
        return FuelBurnNotice(
            code = code,
            label = label,
            pinned = pinned,
            shiftCode = shiftCode,
            stopCount = stopCount,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fuel burn notice in status proposed. */
fun newFuelBurnNotice(code: String, label: String): FuelBurnNotice {
    return FuelBurnNotice(
        code = code,
        label = label,
        pinned = false,
        shiftCode = "",
        stopCount = 0,
        locale = "",
        status = FuelBurnNoticeStatus.PROPOSED,
    )
}
