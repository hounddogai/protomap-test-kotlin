package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute group is in its life cycle. */
enum class DisputeGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a dispute group and its current status. */
data class DisputeGroup(
    var code: String,
    var name: String,
    var windowDays: Int,
    var amount: Double,
    var attributes: MutableMap<String, String>,
    var trailerId: String,
    var weight: Double,
    var margin: Double,
    var status: DisputeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeGroupStatus): Boolean {
        when (status) {
            DisputeGroupStatus.PROPOSED -> return next == DisputeGroupStatus.APPROVED
            DisputeGroupStatus.APPROVED -> return next == DisputeGroupStatus.REJECTED
            DisputeGroupStatus.REJECTED -> return next == DisputeGroupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DisputeGroupStatus) {
        if (!canMoveTo(next)) {
            error("dispute group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((amount - weight) * 100) / 100

    fun snapshot(): DisputeGroup {
        return DisputeGroup(
            code = code,
            name = name,
            windowDays = windowDays,
            amount = amount,
            attributes = attributes,
            trailerId = trailerId,
            weight = weight,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dispute group in status proposed. */
fun newDisputeGroup(code: String, name: String): DisputeGroup {
    return DisputeGroup(
        code = code,
        name = name,
        windowDays = 0,
        amount = 0.0,
        attributes = mutableMapOf<String, String>(),
        trailerId = "",
        weight = 0.0,
        margin = 0.0,
        status = DisputeGroupStatus.PROPOSED,
    )
}
