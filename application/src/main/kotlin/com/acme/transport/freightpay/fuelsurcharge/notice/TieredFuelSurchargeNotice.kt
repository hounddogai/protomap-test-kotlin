package com.acme.transport.freightpay.fuelsurcharge.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered fuel surcharge notice records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a tiered fuel surcharge notice is in its life cycle. */
enum class TieredFuelSurchargeNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered fuel surcharge notice as the fuel surcharge team keeps it. */
data class TieredFuelSurchargeNotice(
    var code: String,
    var label: String,
    var batchSize: Int,
    var quantity: Int,
    var routeCode: String,
    var loadKg: Double,
    var status: TieredFuelSurchargeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelSurchargeNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelSurchargeNoticeStatus): Boolean {
        when (status) {
            TieredFuelSurchargeNoticeStatus.IDLE -> {
                when (next) {
                    TieredFuelSurchargeNoticeStatus.DRAINING, TieredFuelSurchargeNoticeStatus.STOPPED -> return true
                }
                return false
            }
            TieredFuelSurchargeNoticeStatus.WARMING -> return next == TieredFuelSurchargeNoticeStatus.READY
            TieredFuelSurchargeNoticeStatus.READY -> {
                when (next) {
                    TieredFuelSurchargeNoticeStatus.DRAINING, TieredFuelSurchargeNoticeStatus.STOPPED -> return true
                }
                return false
            }
            TieredFuelSurchargeNoticeStatus.DRAINING -> {
                when (next) {
                    TieredFuelSurchargeNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredFuelSurchargeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel surcharge notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tiered fuel surcharge notice"} $code"

    fun weightedValue(): Double = round((batchSize.toDouble() + loadKg) * 100) / 100

    fun snapshot(): TieredFuelSurchargeNotice {
        return TieredFuelSurchargeNotice(
            code = code,
            label = label,
            batchSize = batchSize,
            quantity = quantity,
            routeCode = routeCode,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered fuel surcharge notice in status idle. */
fun newTieredFuelSurchargeNotice(code: String, label: String): TieredFuelSurchargeNotice {
    return TieredFuelSurchargeNotice(
        code = code,
        label = label,
        batchSize = 0,
        quantity = 0,
        routeCode = "",
        loadKg = 0.0,
        status = TieredFuelSurchargeNoticeStatus.IDLE,
    )
}
