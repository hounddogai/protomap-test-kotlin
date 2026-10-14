package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback tender slot is in its life cycle. */
enum class FallbackTenderSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback tender slot as the tender team keeps it. */
data class FallbackTenderSlot(
    var code: String,
    var title: String,
    var locale: String,
    var version: Int,
    var phone: String,
    var margin: Double,
    var lane: String,
    var shiftCode: String,
    var priority: Int,
    var status: FallbackTenderSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTenderSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTenderSlotStatus): Boolean {
        when (status) {
            FallbackTenderSlotStatus.NEW -> return next == FallbackTenderSlotStatus.QUEUED || next == FallbackTenderSlotStatus.DONE
            FallbackTenderSlotStatus.QUEUED -> return next == FallbackTenderSlotStatus.RUNNING || next == FallbackTenderSlotStatus.DONE
            FallbackTenderSlotStatus.RUNNING -> {
                when (next) {
                    FallbackTenderSlotStatus.FAILED -> return true
                }
                return false
            }
            FallbackTenderSlotStatus.DONE -> return next == FallbackTenderSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackTenderSlotStatus) {
        if (!canMoveTo(next)) {
            error("fallback tender slot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round((version.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): FallbackTenderSlot {
        return FallbackTenderSlot(
            code = code,
            title = title,
            locale = locale,
            version = version,
            phone = phone,
            margin = margin,
            lane = lane,
            shiftCode = shiftCode,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback tender slot in status new. */
fun newFallbackTenderSlot(code: String, title: String): FallbackTenderSlot {
    return FallbackTenderSlot(
        code = code,
        title = title,
        locale = "",
        version = 0,
        phone = "",
        margin = 0.0,
        lane = "",
        shiftCode = "",
        priority = 0,
        status = FallbackTenderSlotStatus.NEW,
    )
}
