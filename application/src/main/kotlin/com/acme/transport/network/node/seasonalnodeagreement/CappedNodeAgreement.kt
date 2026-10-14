package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped node agreement records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a capped node agreement is in its life cycle. */
enum class CappedNodeAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One capped node agreement, with what the planners need to act on it. */
data class CappedNodeAgreement(
    var code: String,
    var title: String,
    var attempts: Int,
    var graceDays: Int,
    var trailerId: String,
    var status: CappedNodeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedNodeAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedNodeAgreementStatus): Boolean {
        when (status) {
            CappedNodeAgreementStatus.IDLE -> return next == CappedNodeAgreementStatus.READY
            CappedNodeAgreementStatus.WARMING -> {
                when (next) {
                    CappedNodeAgreementStatus.READY, CappedNodeAgreementStatus.DRAINING -> return true
                }
                return false
            }
            CappedNodeAgreementStatus.READY -> return next == CappedNodeAgreementStatus.DRAINING
            CappedNodeAgreementStatus.DRAINING -> {
                when (next) {
                    CappedNodeAgreementStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedNodeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("capped node agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedLoad(): Double = round(graceDays.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): CappedNodeAgreement {
        return CappedNodeAgreement(
            code = code,
            title = title,
            attempts = attempts,
            graceDays = graceDays,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped node agreement in status idle. */
fun newCappedNodeAgreement(code: String, title: String): CappedNodeAgreement {
    return CappedNodeAgreement(
        code = code,
        title = title,
        attempts = 0,
        graceDays = 0,
        trailerId = "",
        status = CappedNodeAgreementStatus.IDLE,
    )
}
