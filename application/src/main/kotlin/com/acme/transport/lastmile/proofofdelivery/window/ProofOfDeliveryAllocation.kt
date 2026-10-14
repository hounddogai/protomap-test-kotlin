package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.text.normalize

/** Where a proof of delivery allocation is in its life cycle. */
enum class ProofOfDeliveryAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A proof of delivery allocation. Its code is unique within a tenant. */
data class ProofOfDeliveryAllocation(
    var code: String,
    var label: String,
    var rate: Double,
    var checksum: String,
    var dock: String,
    var channel: String,
    var status: ProofOfDeliveryAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryAllocationStatus): Boolean {
        when (status) {
            ProofOfDeliveryAllocationStatus.PROPOSED -> {
                when (next) {
                    ProofOfDeliveryAllocationStatus.APPROVED, ProofOfDeliveryAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliveryAllocationStatus.APPROVED -> {
                when (next) {
                    ProofOfDeliveryAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliveryAllocationStatus.REJECTED -> return next == ProofOfDeliveryAllocationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryAllocationStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = rate

    fun snapshot(): ProofOfDeliveryAllocation {
        return ProofOfDeliveryAllocation(
            code = code,
            label = label,
            rate = rate,
            checksum = checksum,
            dock = dock,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a proof of delivery allocation in status proposed. */
fun newProofOfDeliveryAllocation(code: String, label: String): ProofOfDeliveryAllocation {
    return ProofOfDeliveryAllocation(
        code = code,
        label = label,
        rate = 0.0,
        checksum = "",
        dock = "",
        channel = "",
        status = ProofOfDeliveryAllocationStatus.PROPOSED,
    )
}
