package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted fuel surcharge notice is in its life cycle. */
enum class WeightedFuelSurchargeNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a weighted fuel surcharge notice and its current status. */
data class WeightedFuelSurchargeNotice(
    var code: String,
    var label: String,
    var version: Int,
    var score: Double,
    var checksum: String,
    var discount: Double,
    var status: WeightedFuelSurchargeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelSurchargeNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelSurchargeNoticeStatus): Boolean {
        when (status) {
            WeightedFuelSurchargeNoticeStatus.IDLE -> {
                when (next) {
                    WeightedFuelSurchargeNoticeStatus.READY -> return true
                }
                return false
            }
            WeightedFuelSurchargeNoticeStatus.WARMING -> {
                when (next) {
                    WeightedFuelSurchargeNoticeStatus.STOPPED -> return true
                }
                return false
            }
            WeightedFuelSurchargeNoticeStatus.READY -> return next == WeightedFuelSurchargeNoticeStatus.DRAINING || next == WeightedFuelSurchargeNoticeStatus.STOPPED
            WeightedFuelSurchargeNoticeStatus.DRAINING -> return next == WeightedFuelSurchargeNoticeStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: WeightedFuelSurchargeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel surcharge notice $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"weighted fuel surcharge notice"} $code"

    fun projectedVolume(): Double = round(version.toDouble() * discount * 100) / 100

    fun snapshot(): WeightedFuelSurchargeNotice {
        return WeightedFuelSurchargeNotice(
            code = code,
            label = label,
            version = version,
            score = score,
            checksum = checksum,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted fuel surcharge notice in status idle. */
fun newWeightedFuelSurchargeNotice(code: String, label: String): WeightedFuelSurchargeNotice {
    return WeightedFuelSurchargeNotice(
        code = code,
        label = label,
        version = 0,
        score = 0.0,
        checksum = "",
        discount = 0.0,
        status = WeightedFuelSurchargeNoticeStatus.IDLE,
    )
}
