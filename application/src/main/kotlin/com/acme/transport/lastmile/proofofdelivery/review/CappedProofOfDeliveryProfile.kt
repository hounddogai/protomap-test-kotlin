package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.text.normalize

/** Where a capped proof of delivery profile is in its life cycle. */
enum class CappedProofOfDeliveryProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One capped proof of delivery profile, with what the planners need to act on it. */
data class CappedProofOfDeliveryProfile(
    var code: String,
    var name: String,
    var checksum: String,
    var reviewer: String,
    var driverId: String,
    var capacity: Int,
    var status: CappedProofOfDeliveryProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedProofOfDeliveryProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedProofOfDeliveryProfileStatus): Boolean {
        when (status) {
            CappedProofOfDeliveryProfileStatus.IDLE -> return next == CappedProofOfDeliveryProfileStatus.READY || next == CappedProofOfDeliveryProfileStatus.STOPPED
            CappedProofOfDeliveryProfileStatus.WARMING -> return next == CappedProofOfDeliveryProfileStatus.READY
            CappedProofOfDeliveryProfileStatus.READY -> return next == CappedProofOfDeliveryProfileStatus.STOPPED
            CappedProofOfDeliveryProfileStatus.DRAINING -> return next == CappedProofOfDeliveryProfileStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedProofOfDeliveryProfileStatus) {
        if (!canMoveTo(next)) {
            error("capped proof of delivery profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): CappedProofOfDeliveryProfile {
        return CappedProofOfDeliveryProfile(
            code = code,
            name = name,
            checksum = checksum,
            reviewer = reviewer,
            driverId = driverId,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped proof of delivery profile in status idle. */
fun newCappedProofOfDeliveryProfile(code: String, name: String): CappedProofOfDeliveryProfile {
    return CappedProofOfDeliveryProfile(
        code = code,
        name = name,
        checksum = "",
        reviewer = "",
        driverId = "",
        capacity = 0,
        status = CappedProofOfDeliveryProfileStatus.IDLE,
    )
}
