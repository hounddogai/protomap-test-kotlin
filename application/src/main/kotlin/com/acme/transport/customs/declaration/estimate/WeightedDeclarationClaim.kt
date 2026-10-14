package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.text.normalize

/** Where a weighted declaration claim is in its life cycle. */
enum class WeightedDeclarationClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted declaration claim and its current status. */
data class WeightedDeclarationClaim(
    var code: String,
    var label: String,
    var channel: String,
    var visible: Boolean,
    var driverId: String,
    var quantity: Int,
    var status: WeightedDeclarationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDeclarationClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDeclarationClaimStatus): Boolean {
        when (status) {
            WeightedDeclarationClaimStatus.PLANNED -> return next == WeightedDeclarationClaimStatus.CANCELLED
            WeightedDeclarationClaimStatus.SCHEDULED -> {
                when (next) {
                    WeightedDeclarationClaimStatus.IN_PROGRESS -> return true
                }
                return false
            }
            WeightedDeclarationClaimStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedDeclarationClaimStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedDeclarationClaimStatus.COMPLETED -> return next == WeightedDeclarationClaimStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedDeclarationClaimStatus) {
        if (!canMoveTo(next)) {
            error("weighted declaration claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted declaration claim"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): WeightedDeclarationClaim {
        return WeightedDeclarationClaim(
            code = code,
            label = label,
            channel = channel,
            visible = visible,
            driverId = driverId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted declaration claim in status planned. */
fun newWeightedDeclarationClaim(code: String, label: String): WeightedDeclarationClaim {
    return WeightedDeclarationClaim(
        code = code,
        label = label,
        channel = "",
        visible = false,
        driverId = "",
        quantity = 0,
        status = WeightedDeclarationClaimStatus.PLANNED,
    )
}
