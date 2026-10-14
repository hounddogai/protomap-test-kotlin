package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary clearance checkpoint is in its life cycle. */
enum class PrimaryClearanceCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a primary clearance checkpoint and its current status. */
data class PrimaryClearanceCheckpoint(
    var code: String,
    var name: String,
    var carrier: String,
    var amount: Double,
    var locale: String,
    var attributes: MutableMap<String, String>,
    var country: String,
    var endsAt: Int,
    var status: PrimaryClearanceCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryClearanceCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryClearanceCheckpointStatus): Boolean {
        when (status) {
            PrimaryClearanceCheckpointStatus.IDLE -> return next == PrimaryClearanceCheckpointStatus.DRAINING || next == PrimaryClearanceCheckpointStatus.STOPPED
            PrimaryClearanceCheckpointStatus.WARMING -> {
                when (next) {
                    PrimaryClearanceCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryClearanceCheckpointStatus.READY -> {
                when (next) {
                    PrimaryClearanceCheckpointStatus.DRAINING, PrimaryClearanceCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryClearanceCheckpointStatus.DRAINING -> {
                when (next) {
                    PrimaryClearanceCheckpointStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryClearanceCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("primary clearance checkpoint $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((endsAt.toDouble() + amount) * 100) / 100

    fun snapshot(): PrimaryClearanceCheckpoint {
        return PrimaryClearanceCheckpoint(
            code = code,
            name = name,
            carrier = carrier,
            amount = amount,
            locale = locale,
            attributes = attributes,
            country = country,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary clearance checkpoint in status idle. */
fun newPrimaryClearanceCheckpoint(code: String, name: String): PrimaryClearanceCheckpoint {
    return PrimaryClearanceCheckpoint(
        code = code,
        name = name,
        carrier = "",
        amount = 0.0,
        locale = "",
        attributes = mutableMapOf<String, String>(),
        country = "",
        endsAt = 0,
        status = PrimaryClearanceCheckpointStatus.IDLE,
    )
}
