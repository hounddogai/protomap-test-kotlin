package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker segment is in its life cycle. */
enum class BrokerSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A broker segment. Its code is unique within a tenant. */
data class BrokerSegment(
    var code: String,
    var label: String,
    var ownerId: String,
    var title: String,
    var region: String,
    var routeCode: String,
    var odometer: Int,
    var discount: Double,
    var status: BrokerSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerSegmentStatus): Boolean {
        when (status) {
            BrokerSegmentStatus.REQUESTED -> return next == BrokerSegmentStatus.GRANTED
            BrokerSegmentStatus.GRANTED -> return next == BrokerSegmentStatus.EXPIRED
            BrokerSegmentStatus.REVOKED -> return next == BrokerSegmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BrokerSegmentStatus) {
        if (!canMoveTo(next)) {
            error("broker segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round(discount * odometer.toDouble() * 100) / 100

    fun snapshot(): BrokerSegment {
        return BrokerSegment(
            code = code,
            label = label,
            ownerId = ownerId,
            title = title,
            region = region,
            routeCode = routeCode,
            odometer = odometer,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a broker segment in status requested. */
fun newBrokerSegment(code: String, label: String): BrokerSegment {
    return BrokerSegment(
        code = code,
        label = label,
        ownerId = "",
        title = "",
        region = "",
        routeCode = "",
        odometer = 0,
        discount = 0.0,
        status = BrokerSegmentStatus.REQUESTED,
    )
}
