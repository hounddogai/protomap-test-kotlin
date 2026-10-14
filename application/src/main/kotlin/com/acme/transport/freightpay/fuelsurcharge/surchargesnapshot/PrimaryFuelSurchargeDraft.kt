package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary fuel surcharge draft is in its life cycle. */
enum class PrimaryFuelSurchargeDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary fuel surcharge draft, with what the planners need to act on it. */
data class PrimaryFuelSurchargeDraft(
    var code: String,
    var name: String,
    var shiftCode: String,
    var currency: String,
    var carrier: String,
    var amount: Double,
    var floorValue: Double,
    var threshold: Double,
    var status: PrimaryFuelSurchargeDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFuelSurchargeDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFuelSurchargeDraftStatus): Boolean {
        when (status) {
            PrimaryFuelSurchargeDraftStatus.NEW -> return next == PrimaryFuelSurchargeDraftStatus.FAILED
            PrimaryFuelSurchargeDraftStatus.QUEUED -> return next == PrimaryFuelSurchargeDraftStatus.RUNNING || next == PrimaryFuelSurchargeDraftStatus.FAILED
            PrimaryFuelSurchargeDraftStatus.RUNNING -> {
                when (next) {
                    PrimaryFuelSurchargeDraftStatus.FAILED -> return true
                }
                return false
            }
            PrimaryFuelSurchargeDraftStatus.DONE -> {
                when (next) {
                    PrimaryFuelSurchargeDraftStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryFuelSurchargeDraftStatus) {
        if (!canMoveTo(next)) {
            error("primary fuel surcharge draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round((threshold + floorValue) * 100) / 100

    fun snapshot(): PrimaryFuelSurchargeDraft {
        return PrimaryFuelSurchargeDraft(
            code = code,
            name = name,
            shiftCode = shiftCode,
            currency = currency,
            carrier = carrier,
            amount = amount,
            floorValue = floorValue,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary fuel surcharge draft in status new. */
fun newPrimaryFuelSurchargeDraft(code: String, name: String): PrimaryFuelSurchargeDraft {
    return PrimaryFuelSurchargeDraft(
        code = code,
        name = name,
        shiftCode = "",
        currency = "",
        carrier = "",
        amount = 0.0,
        floorValue = 0.0,
        threshold = 0.0,
        status = PrimaryFuelSurchargeDraftStatus.NEW,
    )
}
