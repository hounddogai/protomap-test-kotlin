package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal fuel surcharge slot records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a seasonal fuel surcharge slot is in its life cycle. */
enum class SeasonalFuelSurchargeSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal fuel surcharge slot. Its code is unique within a tenant. */
data class SeasonalFuelSurchargeSlot(
    var code: String,
    var title: String,
    var visible: Boolean,
    var archived: Boolean,
    var cost: Double,
    var graceDays: Int,
    var checksum: String,
    var status: SeasonalFuelSurchargeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalFuelSurchargeSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalFuelSurchargeSlotStatus): Boolean {
        when (status) {
            SeasonalFuelSurchargeSlotStatus.NEW -> {
                when (next) {
                    SeasonalFuelSurchargeSlotStatus.FAILED -> return true
                }
                return false
            }
            SeasonalFuelSurchargeSlotStatus.QUEUED -> {
                when (next) {
                    SeasonalFuelSurchargeSlotStatus.RUNNING, SeasonalFuelSurchargeSlotStatus.FAILED -> return true
                }
                return false
            }
            SeasonalFuelSurchargeSlotStatus.RUNNING -> {
                when (next) {
                    SeasonalFuelSurchargeSlotStatus.FAILED -> return true
                }
                return false
            }
            SeasonalFuelSurchargeSlotStatus.DONE -> return next == SeasonalFuelSurchargeSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalFuelSurchargeSlotStatus) {
        if (!canMoveTo(next)) {
            error("seasonal fuel surcharge slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedValue(): Double = round((graceDays.toDouble() + cost) * 100) / 100

    fun snapshot(): SeasonalFuelSurchargeSlot {
        return SeasonalFuelSurchargeSlot(
            code = code,
            title = title,
            visible = visible,
            archived = archived,
            cost = cost,
            graceDays = graceDays,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal fuel surcharge slot in status new. */
fun newSeasonalFuelSurchargeSlot(code: String, title: String): SeasonalFuelSurchargeSlot {
    return SeasonalFuelSurchargeSlot(
        code = code,
        title = title,
        visible = false,
        archived = false,
        cost = 0.0,
        graceDays = 0,
        checksum = "",
        status = SeasonalFuelSurchargeSlotStatus.NEW,
    )
}
