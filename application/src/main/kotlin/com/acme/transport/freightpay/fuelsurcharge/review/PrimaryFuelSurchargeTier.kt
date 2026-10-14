package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary fuel surcharge tier is in its life cycle. */
enum class PrimaryFuelSurchargeTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a primary fuel surcharge tier and its current status. */
data class PrimaryFuelSurchargeTier(
    var code: String,
    var label: String,
    var retries: Int,
    var cost: Double,
    var pallets: Int,
    var expiresAt: Int,
    var name: String,
    var status: PrimaryFuelSurchargeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFuelSurchargeTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFuelSurchargeTierStatus): Boolean {
        when (status) {
            PrimaryFuelSurchargeTierStatus.IDLE -> {
                when (next) {
                    PrimaryFuelSurchargeTierStatus.DRAINING, PrimaryFuelSurchargeTierStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryFuelSurchargeTierStatus.WARMING -> return next == PrimaryFuelSurchargeTierStatus.DRAINING || next == PrimaryFuelSurchargeTierStatus.STOPPED
            PrimaryFuelSurchargeTierStatus.READY -> {
                when (next) {
                    PrimaryFuelSurchargeTierStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryFuelSurchargeTierStatus.DRAINING -> return next == PrimaryFuelSurchargeTierStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryFuelSurchargeTierStatus) {
        if (!canMoveTo(next)) {
            error("primary fuel surcharge tier $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary fuel surcharge tier"} $code"

    fun effectiveTotal(): Double = round((retries.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryFuelSurchargeTier {
        return PrimaryFuelSurchargeTier(
            code = code,
            label = label,
            retries = retries,
            cost = cost,
            pallets = pallets,
            expiresAt = expiresAt,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary fuel surcharge tier in status idle. */
fun newPrimaryFuelSurchargeTier(code: String, label: String): PrimaryFuelSurchargeTier {
    return PrimaryFuelSurchargeTier(
        code = code,
        label = label,
        retries = 0,
        cost = 0.0,
        pallets = 0,
        expiresAt = 0,
        name = "",
        status = PrimaryFuelSurchargeTierStatus.IDLE,
    )
}
