package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.normalize

/** Where a drop variance is in its life cycle. */
enum class DropVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One drop variance, with what the planners need to act on it. */
data class DropVariance(
    var code: String,
    var name: String,
    var batchSize: Int,
    var lane: String,
    var depot: String,
    var contactEmail: String,
    var label: String,
    var shiftCode: String,
    var status: DropVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DropVarianceStatus): Boolean {
        when (status) {
            DropVarianceStatus.PROPOSED -> return next == DropVarianceStatus.WITHDRAWN
            DropVarianceStatus.APPROVED -> {
                when (next) {
                    DropVarianceStatus.REJECTED, DropVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            DropVarianceStatus.REJECTED -> {
                when (next) {
                    DropVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropVarianceStatus) {
        if (!canMoveTo(next)) {
            error("drop variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): DropVariance {
        return DropVariance(
            code = code,
            name = name,
            batchSize = batchSize,
            lane = lane,
            depot = depot,
            contactEmail = contactEmail,
            label = label,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a drop variance in status proposed. */
fun newDropVariance(code: String, name: String): DropVariance {
    return DropVariance(
        code = code,
        name = name,
        batchSize = 0,
        lane = "",
        depot = "",
        contactEmail = "",
        label = "",
        shiftCode = "",
        status = DropVarianceStatus.PROPOSED,
    )
}
