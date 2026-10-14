package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery bundle is in its life cycle. */
enum class RedeliveryBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A redelivery bundle as the redelivery team keeps it. */
data class RedeliveryBundle(
    var code: String,
    var name: String,
    var amount: Double,
    var fuelLiters: Double,
    var country: String,
    var lane: String,
    var checksum: String,
    var routeCode: String,
    var pallets: Int,
    var status: RedeliveryBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryBundleStatus): Boolean {
        when (status) {
            RedeliveryBundleStatus.IDLE -> {
                when (next) {
                    RedeliveryBundleStatus.STOPPED -> return true
                }
                return false
            }
            RedeliveryBundleStatus.WARMING -> {
                when (next) {
                    RedeliveryBundleStatus.READY -> return true
                }
                return false
            }
            RedeliveryBundleStatus.READY -> return next == RedeliveryBundleStatus.DRAINING || next == RedeliveryBundleStatus.STOPPED
            RedeliveryBundleStatus.DRAINING -> return next == RedeliveryBundleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RedeliveryBundleStatus) {
        if (!canMoveTo(next)) {
            error("redelivery bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round((pallets.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): RedeliveryBundle {
        return RedeliveryBundle(
            code = code,
            name = name,
            amount = amount,
            fuelLiters = fuelLiters,
            country = country,
            lane = lane,
            checksum = checksum,
            routeCode = routeCode,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a redelivery bundle in status idle. */
fun newRedeliveryBundle(code: String, name: String): RedeliveryBundle {
    return RedeliveryBundle(
        code = code,
        name = name,
        amount = 0.0,
        fuelLiters = 0.0,
        country = "",
        lane = "",
        checksum = "",
        routeCode = "",
        pallets = 0,
        status = RedeliveryBundleStatus.IDLE,
    )
}
