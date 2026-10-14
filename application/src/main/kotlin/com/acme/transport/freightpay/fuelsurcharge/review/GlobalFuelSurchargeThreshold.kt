package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global fuel surcharge threshold is in its life cycle. */
enum class GlobalFuelSurchargeThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One global fuel surcharge threshold, with what the planners need to act on it. */
data class GlobalFuelSurchargeThreshold(
    var code: String,
    var title: String,
    var contactEmail: String,
    var shiftCode: String,
    var version: Int,
    var segment: String,
    var quantity: Int,
    var batchSize: Int,
    var vehicleId: String,
    var source: String,
    var status: GlobalFuelSurchargeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFuelSurchargeThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFuelSurchargeThresholdStatus): Boolean {
        when (status) {
            GlobalFuelSurchargeThresholdStatus.NEW -> {
                when (next) {
                    GlobalFuelSurchargeThresholdStatus.QUEUED, GlobalFuelSurchargeThresholdStatus.RUNNING -> return true
                }
                return false
            }
            GlobalFuelSurchargeThresholdStatus.QUEUED -> {
                when (next) {
                    GlobalFuelSurchargeThresholdStatus.RUNNING, GlobalFuelSurchargeThresholdStatus.FAILED -> return true
                }
                return false
            }
            GlobalFuelSurchargeThresholdStatus.RUNNING -> return next == GlobalFuelSurchargeThresholdStatus.FAILED
            GlobalFuelSurchargeThresholdStatus.DONE -> {
                when (next) {
                    GlobalFuelSurchargeThresholdStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalFuelSurchargeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("global fuel surcharge threshold $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round(batchSize.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): GlobalFuelSurchargeThreshold {
        return GlobalFuelSurchargeThreshold(
            code = code,
            title = title,
            contactEmail = contactEmail,
            shiftCode = shiftCode,
            version = version,
            segment = segment,
            quantity = quantity,
            batchSize = batchSize,
            vehicleId = vehicleId,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a global fuel surcharge threshold in status new. */
fun newGlobalFuelSurchargeThreshold(code: String, title: String): GlobalFuelSurchargeThreshold {
    return GlobalFuelSurchargeThreshold(
        code = code,
        title = title,
        contactEmail = "",
        shiftCode = "",
        version = 0,
        segment = "",
        quantity = 0,
        batchSize = 0,
        vehicleId = "",
        source = "",
        status = GlobalFuelSurchargeThresholdStatus.NEW,
    )
}
