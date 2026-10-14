package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay policy is in its life cycle. */
enum class RelayPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A relay policy. Its code is unique within a tenant. */
data class RelayPolicy(
    var code: String,
    var label: String,
    var endsAt: Int,
    var floorValue: Double,
    var quantity: Int,
    var driverId: String,
    var locale: String,
    var routeCode: String,
    var threshold: Double,
    var title: String,
    var limit: Int,
    var status: RelayPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayPolicyStatus): Boolean {
        when (status) {
            RelayPolicyStatus.OPEN -> return next == RelayPolicyStatus.REOPENED || next == RelayPolicyStatus.ARCHIVED
            RelayPolicyStatus.IN_REVIEW -> return next == RelayPolicyStatus.ARCHIVED
            RelayPolicyStatus.RESOLVED -> return next == RelayPolicyStatus.REOPENED || next == RelayPolicyStatus.ARCHIVED
            RelayPolicyStatus.REOPENED -> {
                when (next) {
                    RelayPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayPolicyStatus) {
        if (!canMoveTo(next)) {
            error("relay policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedValue(): Double = round(quantity.toDouble() * floorValue * 100) / 100

    fun snapshot(): RelayPolicy {
        return RelayPolicy(
            code = code,
            label = label,
            endsAt = endsAt,
            floorValue = floorValue,
            quantity = quantity,
            driverId = driverId,
            locale = locale,
            routeCode = routeCode,
            threshold = threshold,
            title = title,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a relay policy in status open. */
fun newRelayPolicy(code: String, label: String): RelayPolicy {
    return RelayPolicy(
        code = code,
        label = label,
        endsAt = 0,
        floorValue = 0.0,
        quantity = 0,
        driverId = "",
        locale = "",
        routeCode = "",
        threshold = 0.0,
        title = "",
        limit = 0,
        status = RelayPolicyStatus.OPEN,
    )
}
