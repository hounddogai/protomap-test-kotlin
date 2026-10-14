package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.text.normalize

/** Where a manual cutoff claim is in its life cycle. */
enum class ManualCutoffClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual cutoff claim as the cutoff team keeps it. */
data class ManualCutoffClaim(
    var code: String,
    var label: String,
    var driverId: String,
    var visible: Boolean,
    var externalRef: String,
    var title: String,
    var amount: Double,
    var status: ManualCutoffClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCutoffClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCutoffClaimStatus): Boolean {
        when (status) {
            ManualCutoffClaimStatus.PROPOSED -> return next == ManualCutoffClaimStatus.WITHDRAWN
            ManualCutoffClaimStatus.APPROVED -> return next == ManualCutoffClaimStatus.WITHDRAWN
            ManualCutoffClaimStatus.REJECTED -> return next == ManualCutoffClaimStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualCutoffClaimStatus) {
        if (!canMoveTo(next)) {
            error("manual cutoff claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = amount

    fun snapshot(): ManualCutoffClaim {
        return ManualCutoffClaim(
            code = code,
            label = label,
            driverId = driverId,
            visible = visible,
            externalRef = externalRef,
            title = title,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual cutoff claim in status proposed. */
fun newManualCutoffClaim(code: String, label: String): ManualCutoffClaim {
    return ManualCutoffClaim(
        code = code,
        label = label,
        driverId = "",
        visible = false,
        externalRef = "",
        title = "",
        amount = 0.0,
        status = ManualCutoffClaimStatus.PROPOSED,
    )
}
