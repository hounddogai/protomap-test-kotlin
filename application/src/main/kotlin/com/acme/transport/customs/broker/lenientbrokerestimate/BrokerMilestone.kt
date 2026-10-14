package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker milestone is in its life cycle. */
enum class BrokerMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A broker milestone. Its code is unique within a tenant. */
data class BrokerMilestone(
    var code: String,
    var name: String,
    var ownerId: String,
    var ceiling: Double,
    var notes: String,
    var label: String,
    var weight: Double,
    var segment: String,
    var graceDays: Int,
    var status: BrokerMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerMilestoneStatus): Boolean {
        when (status) {
            BrokerMilestoneStatus.PROPOSED -> return next == BrokerMilestoneStatus.REJECTED || next == BrokerMilestoneStatus.WITHDRAWN
            BrokerMilestoneStatus.APPROVED -> {
                when (next) {
                    BrokerMilestoneStatus.WITHDRAWN -> return true
                }
                return false
            }
            BrokerMilestoneStatus.REJECTED -> {
                when (next) {
                    BrokerMilestoneStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("broker milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun effectiveVolume(): Double = round(weight * ceiling * 100) / 100

    fun snapshot(): BrokerMilestone {
        return BrokerMilestone(
            code = code,
            name = name,
            ownerId = ownerId,
            ceiling = ceiling,
            notes = notes,
            label = label,
            weight = weight,
            segment = segment,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a broker milestone in status proposed. */
fun newBrokerMilestone(code: String, name: String): BrokerMilestone {
    return BrokerMilestone(
        code = code,
        name = name,
        ownerId = "",
        ceiling = 0.0,
        notes = "",
        label = "",
        weight = 0.0,
        segment = "",
        graceDays = 0,
        status = BrokerMilestoneStatus.PROPOSED,
    )
}
