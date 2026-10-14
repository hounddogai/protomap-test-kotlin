package com.acme.transport.fleetops.leasereturn.returnrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lease return window is in its life cycle. */
enum class AdaptiveLeaseReturnWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive lease return window as the lease return team keeps it. */
data class AdaptiveLeaseReturnWindow(
    var code: String,
    var name: String,
    var amount: Double,
    var endsAt: Int,
    var attributes: MutableMap<String, String>,
    var cost: Double,
    var unit: String,
    var capacity: Int,
    var tags: MutableList<String>,
    var minimum: Double,
    var status: AdaptiveLeaseReturnWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLeaseReturnWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLeaseReturnWindowStatus): Boolean {
        when (status) {
            AdaptiveLeaseReturnWindowStatus.PROPOSED -> return next == AdaptiveLeaseReturnWindowStatus.REJECTED
            AdaptiveLeaseReturnWindowStatus.APPROVED -> return next == AdaptiveLeaseReturnWindowStatus.REJECTED || next == AdaptiveLeaseReturnWindowStatus.WITHDRAWN
            AdaptiveLeaseReturnWindowStatus.REJECTED -> return next == AdaptiveLeaseReturnWindowStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveLeaseReturnWindowStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lease return window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((amount + cost) * 100) / 100

    fun snapshot(): AdaptiveLeaseReturnWindow {
        return AdaptiveLeaseReturnWindow(
            code = code,
            name = name,
            amount = amount,
            endsAt = endsAt,
            attributes = attributes,
            cost = cost,
            unit = unit,
            capacity = capacity,
            tags = tags,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive lease return window in status proposed. */
fun newAdaptiveLeaseReturnWindow(code: String, name: String): AdaptiveLeaseReturnWindow {
    return AdaptiveLeaseReturnWindow(
        code = code,
        name = name,
        amount = 0.0,
        endsAt = 0,
        attributes = mutableMapOf<String, String>(),
        cost = 0.0,
        unit = "",
        capacity = 0,
        tags = mutableListOf<String>(),
        minimum = 0.0,
        status = AdaptiveLeaseReturnWindowStatus.PROPOSED,
    )
}
