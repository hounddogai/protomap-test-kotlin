package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge notice is in its life cycle. */
enum class FuelSurchargeNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fuel surcharge notice. Its code is unique within a tenant. */
data class FuelSurchargeNotice(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var loadKg: Double,
    var createdAt: Int,
    var status: FuelSurchargeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeNoticeStatus): Boolean {
        when (status) {
            FuelSurchargeNoticeStatus.PLANNED -> return next == FuelSurchargeNoticeStatus.SCHEDULED
            FuelSurchargeNoticeStatus.SCHEDULED -> {
                when (next) {
                    FuelSurchargeNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            FuelSurchargeNoticeStatus.IN_PROGRESS -> return next == FuelSurchargeNoticeStatus.COMPLETED || next == FuelSurchargeNoticeStatus.CANCELLED
            FuelSurchargeNoticeStatus.COMPLETED -> return next == FuelSurchargeNoticeStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round((createdAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): FuelSurchargeNotice {
        return FuelSurchargeNotice(
            code = code,
            title = title,
            pinned = pinned,
            loadKg = loadKg,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel surcharge notice in status planned. */
fun newFuelSurchargeNotice(code: String, title: String): FuelSurchargeNotice {
    return FuelSurchargeNotice(
        code = code,
        title = title,
        pinned = false,
        loadKg = 0.0,
        createdAt = 0,
        status = FuelSurchargeNoticeStatus.PLANNED,
    )
}
