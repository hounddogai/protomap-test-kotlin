package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global redelivery bundle is in its life cycle. */
enum class GlobalRedeliveryBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a global redelivery bundle and its current status. */
data class GlobalRedeliveryBundle(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var unit: String,
    var capacity: Int,
    var ratio: Double,
    var endsAt: Int,
    var ownerId: String,
    var rate: Double,
    var carrier: String,
    var vehicleId: String,
    var status: GlobalRedeliveryBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRedeliveryBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRedeliveryBundleStatus): Boolean {
        when (status) {
            GlobalRedeliveryBundleStatus.PROPOSED -> {
                when (next) {
                    GlobalRedeliveryBundleStatus.APPROVED -> return true
                }
                return false
            }
            GlobalRedeliveryBundleStatus.APPROVED -> return next == GlobalRedeliveryBundleStatus.WITHDRAWN
            GlobalRedeliveryBundleStatus.REJECTED -> return next == GlobalRedeliveryBundleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalRedeliveryBundleStatus) {
        if (!canMoveTo(next)) {
            error("global redelivery bundle $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"global redelivery bundle"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round(ratio * endsAt.toDouble() * 100) / 100

    fun snapshot(): GlobalRedeliveryBundle {
        return GlobalRedeliveryBundle(
            code = code,
            label = label,
            attributes = attributes,
            unit = unit,
            capacity = capacity,
            ratio = ratio,
            endsAt = endsAt,
            ownerId = ownerId,
            rate = rate,
            carrier = carrier,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a global redelivery bundle in status proposed. */
fun newGlobalRedeliveryBundle(code: String, label: String): GlobalRedeliveryBundle {
    return GlobalRedeliveryBundle(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        unit = "",
        capacity = 0,
        ratio = 0.0,
        endsAt = 0,
        ownerId = "",
        rate = 0.0,
        carrier = "",
        vehicleId = "",
        status = GlobalRedeliveryBundleStatus.PROPOSED,
    )
}
