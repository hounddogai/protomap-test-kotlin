package com.acme.transport.emissions.idling.proposal

import com.acme.transport.kit.text.normalize

/** Where a idling allocation is in its life cycle. */
enum class IdlingAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A idling allocation as the idling team keeps it. */
data class IdlingAllocation(
    var code: String,
    var label: String,
    var country: String,
    var score: Double,
    var region: String,
    var status: IdlingAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingAllocationStatus): Boolean {
        when (status) {
            IdlingAllocationStatus.REQUESTED -> {
                when (next) {
                    IdlingAllocationStatus.GRANTED, IdlingAllocationStatus.REVOKED -> return true
                }
                return false
            }
            IdlingAllocationStatus.GRANTED -> return next == IdlingAllocationStatus.EXPIRED
            IdlingAllocationStatus.REVOKED -> return next == IdlingAllocationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: IdlingAllocationStatus) {
        if (!canMoveTo(next)) {
            error("idling allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = score

    fun snapshot(): IdlingAllocation {
        return IdlingAllocation(
            code = code,
            label = label,
            country = country,
            score = score,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a idling allocation in status requested. */
fun newIdlingAllocation(code: String, label: String): IdlingAllocation {
    return IdlingAllocation(
        code = code,
        label = label,
        country = "",
        score = 0.0,
        region = "",
        status = IdlingAllocationStatus.REQUESTED,
    )
}
