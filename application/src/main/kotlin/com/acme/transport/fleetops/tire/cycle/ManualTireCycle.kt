package com.acme.transport.fleetops.tire.cycle

import com.acme.transport.kit.text.normalize

// The manual tire cycle records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a manual tire cycle is in its life cycle. */
enum class ManualTireCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual tire cycle. Its code is unique within a tenant. */
data class ManualTireCycle(
    var code: String,
    var name: String,
    var label: String,
    var lane: String,
    var phone: String,
    var reviewer: String,
    var discount: Double,
    var status: ManualTireCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTireCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTireCycleStatus): Boolean {
        when (status) {
            ManualTireCycleStatus.PROPOSED -> return next == ManualTireCycleStatus.REJECTED
            ManualTireCycleStatus.APPROVED -> {
                when (next) {
                    ManualTireCycleStatus.REJECTED -> return true
                }
                return false
            }
            ManualTireCycleStatus.REJECTED -> return next == ManualTireCycleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualTireCycleStatus) {
        if (!canMoveTo(next)) {
            error("manual tire cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual tire cycle"} $code"

    fun measuredValue(): Double = discount

    fun snapshot(): ManualTireCycle {
        return ManualTireCycle(
            code = code,
            name = name,
            label = label,
            lane = lane,
            phone = phone,
            reviewer = reviewer,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual tire cycle in status proposed. */
fun newManualTireCycle(code: String, name: String): ManualTireCycle {
    return ManualTireCycle(
        code = code,
        name = name,
        label = "",
        lane = "",
        phone = "",
        reviewer = "",
        discount = 0.0,
        status = ManualTireCycleStatus.PROPOSED,
    )
}
