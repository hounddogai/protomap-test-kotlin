package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary fuel surcharge bundle is in its life cycle. */
enum class PrimaryFuelSurchargeBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary fuel surcharge bundle. Its code is unique within a tenant. */
data class PrimaryFuelSurchargeBundle(
    var code: String,
    var label: String,
    var score: Double,
    var visible: Boolean,
    var channel: String,
    var batchSize: Int,
    var status: PrimaryFuelSurchargeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFuelSurchargeBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFuelSurchargeBundleStatus): Boolean {
        when (status) {
            PrimaryFuelSurchargeBundleStatus.IDLE -> {
                when (next) {
                    PrimaryFuelSurchargeBundleStatus.DRAINING -> return true
                }
                return false
            }
            PrimaryFuelSurchargeBundleStatus.WARMING -> return next == PrimaryFuelSurchargeBundleStatus.STOPPED
            PrimaryFuelSurchargeBundleStatus.READY -> return next == PrimaryFuelSurchargeBundleStatus.DRAINING || next == PrimaryFuelSurchargeBundleStatus.STOPPED
            PrimaryFuelSurchargeBundleStatus.DRAINING -> {
                when (next) {
                    PrimaryFuelSurchargeBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryFuelSurchargeBundleStatus) {
        if (!canMoveTo(next)) {
            error("primary fuel surcharge bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round((score - batchSize.toDouble()) * 100) / 100

    fun snapshot(): PrimaryFuelSurchargeBundle {
        return PrimaryFuelSurchargeBundle(
            code = code,
            label = label,
            score = score,
            visible = visible,
            channel = channel,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary fuel surcharge bundle in status idle. */
fun newPrimaryFuelSurchargeBundle(code: String, label: String): PrimaryFuelSurchargeBundle {
    return PrimaryFuelSurchargeBundle(
        code = code,
        label = label,
        score = 0.0,
        visible = false,
        channel = "",
        batchSize = 0,
        status = PrimaryFuelSurchargeBundleStatus.IDLE,
    )
}
