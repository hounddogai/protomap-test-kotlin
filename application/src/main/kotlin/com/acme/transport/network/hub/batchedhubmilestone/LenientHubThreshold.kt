package com.acme.transport.network.hub.batchedhubmilestone

import com.acme.transport.kit.text.normalize

/** Where a lenient hub threshold is in its life cycle. */
enum class LenientHubThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient hub threshold as the hub team keeps it. */
data class LenientHubThreshold(
    var code: String,
    var name: String,
    var label: String,
    var ownerId: String,
    var archived: Boolean,
    var amount: Double,
    var status: LenientHubThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientHubThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientHubThresholdStatus): Boolean {
        when (status) {
            LenientHubThresholdStatus.PROPOSED -> return next == LenientHubThresholdStatus.WITHDRAWN
            LenientHubThresholdStatus.APPROVED -> return next == LenientHubThresholdStatus.REJECTED || next == LenientHubThresholdStatus.WITHDRAWN
            LenientHubThresholdStatus.REJECTED -> return next == LenientHubThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientHubThresholdStatus) {
        if (!canMoveTo(next)) {
            error("lenient hub threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun measuredValue(): Double = amount

    fun snapshot(): LenientHubThreshold {
        return LenientHubThreshold(
            code = code,
            name = name,
            label = label,
            ownerId = ownerId,
            archived = archived,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient hub threshold in status proposed. */
fun newLenientHubThreshold(code: String, name: String): LenientHubThreshold {
    return LenientHubThreshold(
        code = code,
        name = name,
        label = "",
        ownerId = "",
        archived = false,
        amount = 0.0,
        status = LenientHubThresholdStatus.PROPOSED,
    )
}
