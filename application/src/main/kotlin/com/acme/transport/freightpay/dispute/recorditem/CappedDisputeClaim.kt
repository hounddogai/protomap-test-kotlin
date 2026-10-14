package com.acme.transport.freightpay.dispute.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped dispute claim is in its life cycle. */
enum class CappedDisputeClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a capped dispute claim and its current status. */
data class CappedDisputeClaim(
    var code: String,
    var label: String,
    var ratio: Double,
    var expiresAt: Int,
    var routeCode: String,
    var carrier: String,
    var description: String,
    var pallets: Int,
    var archived: Boolean,
    var externalRef: String,
    var quantity: Int,
    var status: CappedDisputeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDisputeClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDisputeClaimStatus): Boolean {
        when (status) {
            CappedDisputeClaimStatus.NEW -> {
                when (next) {
                    CappedDisputeClaimStatus.RUNNING, CappedDisputeClaimStatus.DONE -> return true
                }
                return false
            }
            CappedDisputeClaimStatus.QUEUED -> {
                when (next) {
                    CappedDisputeClaimStatus.DONE -> return true
                }
                return false
            }
            CappedDisputeClaimStatus.RUNNING -> return next == CappedDisputeClaimStatus.FAILED
            CappedDisputeClaimStatus.DONE -> return next == CappedDisputeClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedDisputeClaimStatus) {
        if (!canMoveTo(next)) {
            error("capped dispute claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netExposure(): Double = round(pallets.toDouble() * ratio * 100) / 100

    fun snapshot(): CappedDisputeClaim {
        return CappedDisputeClaim(
            code = code,
            label = label,
            ratio = ratio,
            expiresAt = expiresAt,
            routeCode = routeCode,
            carrier = carrier,
            description = description,
            pallets = pallets,
            archived = archived,
            externalRef = externalRef,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped dispute claim in status new. */
fun newCappedDisputeClaim(code: String, label: String): CappedDisputeClaim {
    return CappedDisputeClaim(
        code = code,
        label = label,
        ratio = 0.0,
        expiresAt = 0,
        routeCode = "",
        carrier = "",
        description = "",
        pallets = 0,
        archived = false,
        externalRef = "",
        quantity = 0,
        status = CappedDisputeClaimStatus.NEW,
    )
}
