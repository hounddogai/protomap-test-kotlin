package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary clearance group is in its life cycle. */
enum class PrimaryClearanceGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a primary clearance group and its current status. */
data class PrimaryClearanceGroup(
    var code: String,
    var name: String,
    var quantity: Int,
    var rate: Double,
    var priority: Int,
    var startsAt: Int,
    var label: String,
    var windowDays: Int,
    var externalRef: String,
    var carrier: String,
    var budget: Double,
    var status: PrimaryClearanceGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryClearanceGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryClearanceGroupStatus): Boolean {
        when (status) {
            PrimaryClearanceGroupStatus.PROPOSED -> {
                when (next) {
                    PrimaryClearanceGroupStatus.REJECTED -> return true
                }
                return false
            }
            PrimaryClearanceGroupStatus.APPROVED -> return next == PrimaryClearanceGroupStatus.REJECTED || next == PrimaryClearanceGroupStatus.WITHDRAWN
            PrimaryClearanceGroupStatus.REJECTED -> {
                when (next) {
                    PrimaryClearanceGroupStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryClearanceGroupStatus) {
        if (!canMoveTo(next)) {
            error("primary clearance group $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun adjustedTotal(): Double = round((priority.toDouble() - rate) * 100) / 100

    fun snapshot(): PrimaryClearanceGroup {
        return PrimaryClearanceGroup(
            code = code,
            name = name,
            quantity = quantity,
            rate = rate,
            priority = priority,
            startsAt = startsAt,
            label = label,
            windowDays = windowDays,
            externalRef = externalRef,
            carrier = carrier,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary clearance group in status proposed. */
fun newPrimaryClearanceGroup(code: String, name: String): PrimaryClearanceGroup {
    return PrimaryClearanceGroup(
        code = code,
        name = name,
        quantity = 0,
        rate = 0.0,
        priority = 0,
        startsAt = 0,
        label = "",
        windowDays = 0,
        externalRef = "",
        carrier = "",
        budget = 0.0,
        status = PrimaryClearanceGroupStatus.PROPOSED,
    )
}
