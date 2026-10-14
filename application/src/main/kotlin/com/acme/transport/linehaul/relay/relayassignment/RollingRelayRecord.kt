package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling relay record is in its life cycle. */
enum class RollingRelayRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rolling relay record and its current status. */
data class RollingRelayRecord(
    var code: String,
    var title: String,
    var region: String,
    var quantity: Int,
    var ceiling: Double,
    var routeCode: String,
    var status: RollingRelayRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRelayRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRelayRecordStatus): Boolean {
        when (status) {
            RollingRelayRecordStatus.REQUESTED -> return next == RollingRelayRecordStatus.REVOKED
            RollingRelayRecordStatus.GRANTED -> return next == RollingRelayRecordStatus.REVOKED || next == RollingRelayRecordStatus.EXPIRED
            RollingRelayRecordStatus.REVOKED -> return next == RollingRelayRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingRelayRecordStatus) {
        if (!canMoveTo(next)) {
            error("rolling relay record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling relay record"} $code"

    fun effectiveLoad(): Double = round(ceiling * quantity.toDouble() * 100) / 100

    fun snapshot(): RollingRelayRecord {
        return RollingRelayRecord(
            code = code,
            title = title,
            region = region,
            quantity = quantity,
            ceiling = ceiling,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling relay record in status requested. */
fun newRollingRelayRecord(code: String, title: String): RollingRelayRecord {
    return RollingRelayRecord(
        code = code,
        title = title,
        region = "",
        quantity = 0,
        ceiling = 0.0,
        routeCode = "",
        status = RollingRelayRecordStatus.REQUESTED,
    )
}
