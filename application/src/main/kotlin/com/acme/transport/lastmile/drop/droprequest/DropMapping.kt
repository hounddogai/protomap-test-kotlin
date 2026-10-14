package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop mapping is in its life cycle. */
enum class DropMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a drop mapping and its current status. */
data class DropMapping(
    var code: String,
    var label: String,
    var trailerId: String,
    var depot: String,
    var pallets: Int,
    var priority: Int,
    var weight: Double,
    var quantity: Int,
    var stopCount: Int,
    var status: DropMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropMappingStatus): Boolean {
        when (status) {
            DropMappingStatus.REQUESTED -> {
                when (next) {
                    DropMappingStatus.GRANTED, DropMappingStatus.REVOKED -> return true
                }
                return false
            }
            DropMappingStatus.GRANTED -> {
                when (next) {
                    DropMappingStatus.REVOKED, DropMappingStatus.EXPIRED -> return true
                }
                return false
            }
            DropMappingStatus.REVOKED -> {
                when (next) {
                    DropMappingStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropMappingStatus) {
        if (!canMoveTo(next)) {
            error("drop mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"drop mapping"} $code"

    fun effectiveExposure(): Double = round((stopCount.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): DropMapping {
        return DropMapping(
            code = code,
            label = label,
            trailerId = trailerId,
            depot = depot,
            pallets = pallets,
            priority = priority,
            weight = weight,
            quantity = quantity,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a drop mapping in status requested. */
fun newDropMapping(code: String, label: String): DropMapping {
    return DropMapping(
        code = code,
        label = label,
        trailerId = "",
        depot = "",
        pallets = 0,
        priority = 0,
        weight = 0.0,
        quantity = 0,
        stopCount = 0,
        status = DropMappingStatus.REQUESTED,
    )
}
