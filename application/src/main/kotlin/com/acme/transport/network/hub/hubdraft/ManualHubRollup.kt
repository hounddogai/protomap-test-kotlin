package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual hub rollup is in its life cycle. */
enum class ManualHubRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual hub rollup. Its code is unique within a tenant. */
data class ManualHubRollup(
    var code: String,
    var name: String,
    var graceDays: Int,
    var floorValue: Double,
    var retries: Int,
    var limit: Int,
    var quantity: Int,
    var endsAt: Int,
    var shiftCode: String,
    var amount: Double,
    var visible: Boolean,
    var status: ManualHubRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualHubRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualHubRollupStatus): Boolean {
        when (status) {
            ManualHubRollupStatus.REQUESTED -> return next == ManualHubRollupStatus.REVOKED
            ManualHubRollupStatus.GRANTED -> return next == ManualHubRollupStatus.EXPIRED
            ManualHubRollupStatus.REVOKED -> return next == ManualHubRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ManualHubRollupStatus) {
        if (!canMoveTo(next)) {
            error("manual hub rollup $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netLoad(): Double = round(floorValue * endsAt.toDouble() * 100) / 100

    fun snapshot(): ManualHubRollup {
        return ManualHubRollup(
            code = code,
            name = name,
            graceDays = graceDays,
            floorValue = floorValue,
            retries = retries,
            limit = limit,
            quantity = quantity,
            endsAt = endsAt,
            shiftCode = shiftCode,
            amount = amount,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual hub rollup in status requested. */
fun newManualHubRollup(code: String, name: String): ManualHubRollup {
    return ManualHubRollup(
        code = code,
        name = name,
        graceDays = 0,
        floorValue = 0.0,
        retries = 0,
        limit = 0,
        quantity = 0,
        endsAt = 0,
        shiftCode = "",
        amount = 0.0,
        visible = false,
        status = ManualHubRollupStatus.REQUESTED,
    )
}
