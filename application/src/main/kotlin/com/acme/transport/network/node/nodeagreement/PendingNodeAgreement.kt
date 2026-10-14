package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending node agreement records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a pending node agreement is in its life cycle. */
enum class PendingNodeAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending node agreement, with what the planners need to act on it. */
data class PendingNodeAgreement(
    var code: String,
    var label: String,
    var lane: String,
    var phone: String,
    var createdAt: Int,
    var checksum: String,
    var amount: Double,
    var status: PendingNodeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNodeAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNodeAgreementStatus): Boolean {
        when (status) {
            PendingNodeAgreementStatus.REQUESTED -> return next == PendingNodeAgreementStatus.EXPIRED
            PendingNodeAgreementStatus.GRANTED -> return next == PendingNodeAgreementStatus.REVOKED || next == PendingNodeAgreementStatus.EXPIRED
            PendingNodeAgreementStatus.REVOKED -> return next == PendingNodeAgreementStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingNodeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("pending node agreement $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveTotal(): Double = round(createdAt.toDouble() * amount * 100) / 100

    fun snapshot(): PendingNodeAgreement {
        return PendingNodeAgreement(
            code = code,
            label = label,
            lane = lane,
            phone = phone,
            createdAt = createdAt,
            checksum = checksum,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending node agreement in status requested. */
fun newPendingNodeAgreement(code: String, label: String): PendingNodeAgreement {
    return PendingNodeAgreement(
        code = code,
        label = label,
        lane = "",
        phone = "",
        createdAt = 0,
        checksum = "",
        amount = 0.0,
        status = PendingNodeAgreementStatus.REQUESTED,
    )
}
