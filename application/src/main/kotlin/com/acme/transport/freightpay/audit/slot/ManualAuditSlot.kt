package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual audit slot records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a manual audit slot is in its life cycle. */
enum class ManualAuditSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One manual audit slot, with what the planners need to act on it. */
data class ManualAuditSlot(
    var code: String,
    var title: String,
    var endsAt: Int,
    var visible: Boolean,
    var carrier: String,
    var contactEmail: String,
    var routeCode: String,
    var amount: Double,
    var distanceKm: Double,
    var status: ManualAuditSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAuditSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAuditSlotStatus): Boolean {
        when (status) {
            ManualAuditSlotStatus.OPEN -> {
                when (next) {
                    ManualAuditSlotStatus.IN_REVIEW, ManualAuditSlotStatus.REOPENED -> return true
                }
                return false
            }
            ManualAuditSlotStatus.IN_REVIEW -> {
                when (next) {
                    ManualAuditSlotStatus.REOPENED, ManualAuditSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualAuditSlotStatus.RESOLVED -> {
                when (next) {
                    ManualAuditSlotStatus.REOPENED -> return true
                }
                return false
            }
            ManualAuditSlotStatus.REOPENED -> {
                when (next) {
                    ManualAuditSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAuditSlotStatus) {
        if (!canMoveTo(next)) {
            error("manual audit slot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"manual audit slot"} $code"

    fun projectedVolume(): Double = round(endsAt.toDouble() * distanceKm * 100) / 100

    fun snapshot(): ManualAuditSlot {
        return ManualAuditSlot(
            code = code,
            title = title,
            endsAt = endsAt,
            visible = visible,
            carrier = carrier,
            contactEmail = contactEmail,
            routeCode = routeCode,
            amount = amount,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual audit slot in status open. */
fun newManualAuditSlot(code: String, title: String): ManualAuditSlot {
    return ManualAuditSlot(
        code = code,
        title = title,
        endsAt = 0,
        visible = false,
        carrier = "",
        contactEmail = "",
        routeCode = "",
        amount = 0.0,
        distanceKm = 0.0,
        status = ManualAuditSlotStatus.OPEN,
    )
}
