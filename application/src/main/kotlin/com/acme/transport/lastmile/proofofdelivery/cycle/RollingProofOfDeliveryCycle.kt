package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.kit.text.normalize

// The rolling proof of delivery cycle records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a rolling proof of delivery cycle is in its life cycle. */
enum class RollingProofOfDeliveryCycleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling proof of delivery cycle as the proof of delivery team keeps it. */
data class RollingProofOfDeliveryCycle(
    var code: String,
    var title: String,
    var externalRef: String,
    var ceiling: Double,
    var depot: String,
    var status: RollingProofOfDeliveryCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingProofOfDeliveryCycleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingProofOfDeliveryCycleStatus): Boolean {
        when (status) {
            RollingProofOfDeliveryCycleStatus.NEW -> {
                when (next) {
                    RollingProofOfDeliveryCycleStatus.RUNNING, RollingProofOfDeliveryCycleStatus.FAILED -> return true
                }
                return false
            }
            RollingProofOfDeliveryCycleStatus.QUEUED -> return next == RollingProofOfDeliveryCycleStatus.DONE || next == RollingProofOfDeliveryCycleStatus.FAILED
            RollingProofOfDeliveryCycleStatus.RUNNING -> return next == RollingProofOfDeliveryCycleStatus.FAILED
            RollingProofOfDeliveryCycleStatus.DONE -> return next == RollingProofOfDeliveryCycleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingProofOfDeliveryCycleStatus) {
        if (!canMoveTo(next)) {
            error("rolling proof of delivery cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling proof of delivery cycle"} $code"

    fun measuredValue(): Double = ceiling

    fun snapshot(): RollingProofOfDeliveryCycle {
        return RollingProofOfDeliveryCycle(
            code = code,
            title = title,
            externalRef = externalRef,
            ceiling = ceiling,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling proof of delivery cycle in status new. */
fun newRollingProofOfDeliveryCycle(code: String, title: String): RollingProofOfDeliveryCycle {
    return RollingProofOfDeliveryCycle(
        code = code,
        title = title,
        externalRef = "",
        ceiling = 0.0,
        depot = "",
        status = RollingProofOfDeliveryCycleStatus.NEW,
    )
}
