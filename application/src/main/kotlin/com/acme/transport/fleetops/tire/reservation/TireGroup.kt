package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire group is in its life cycle. */
enum class TireGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tire group. Its code is unique within a tenant. */
data class TireGroup(
    var code: String,
    var name: String,
    var visible: Boolean,
    var checksum: String,
    var trailerId: String,
    var quantity: Int,
    var shiftCode: String,
    var depot: String,
    var margin: Double,
    var rate: Double,
    var status: TireGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireGroupStatus): Boolean {
        when (status) {
            TireGroupStatus.REQUESTED -> return next == TireGroupStatus.GRANTED || next == TireGroupStatus.EXPIRED
            TireGroupStatus.GRANTED -> return next == TireGroupStatus.REVOKED || next == TireGroupStatus.EXPIRED
            TireGroupStatus.REVOKED -> return next == TireGroupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TireGroupStatus) {
        if (!canMoveTo(next)) {
            error("tire group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveTotal(): Double = round(margin * quantity.toDouble() * 100) / 100

    fun snapshot(): TireGroup {
        return TireGroup(
            code = code,
            name = name,
            visible = visible,
            checksum = checksum,
            trailerId = trailerId,
            quantity = quantity,
            shiftCode = shiftCode,
            depot = depot,
            margin = margin,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tire group in status requested. */
fun newTireGroup(code: String, name: String): TireGroup {
    return TireGroup(
        code = code,
        name = name,
        visible = false,
        checksum = "",
        trailerId = "",
        quantity = 0,
        shiftCode = "",
        depot = "",
        margin = 0.0,
        rate = 0.0,
        status = TireGroupStatus.REQUESTED,
    )
}
