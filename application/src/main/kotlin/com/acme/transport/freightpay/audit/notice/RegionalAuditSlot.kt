package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional audit slot is in its life cycle. */
enum class RegionalAuditSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One regional audit slot, with what the planners need to act on it. */
data class RegionalAuditSlot(
    var code: String,
    var title: String,
    var shiftCode: String,
    var odometer: Int,
    var cost: Double,
    var windowDays: Int,
    var status: RegionalAuditSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAuditSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAuditSlotStatus): Boolean {
        when (status) {
            RegionalAuditSlotStatus.REQUESTED -> return next == RegionalAuditSlotStatus.REVOKED || next == RegionalAuditSlotStatus.EXPIRED
            RegionalAuditSlotStatus.GRANTED -> return next == RegionalAuditSlotStatus.EXPIRED
            RegionalAuditSlotStatus.REVOKED -> {
                when (next) {
                    RegionalAuditSlotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalAuditSlotStatus) {
        if (!canMoveTo(next)) {
            error("regional audit slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional audit slot"} $code"

    fun netVolume(): Double = round(cost * windowDays.toDouble() * 100) / 100

    fun snapshot(): RegionalAuditSlot {
        return RegionalAuditSlot(
            code = code,
            title = title,
            shiftCode = shiftCode,
            odometer = odometer,
            cost = cost,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional audit slot in status requested. */
fun newRegionalAuditSlot(code: String, title: String): RegionalAuditSlot {
    return RegionalAuditSlot(
        code = code,
        title = title,
        shiftCode = "",
        odometer = 0,
        cost = 0.0,
        windowDays = 0,
        status = RegionalAuditSlotStatus.REQUESTED,
    )
}
