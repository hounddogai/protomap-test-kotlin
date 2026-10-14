package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge batch is in its life cycle. */
enum class EvChargeBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a ev charge batch and its current status. */
data class EvChargeBatch(
    var code: String,
    var name: String,
    var ownerId: String,
    var region: String,
    var priority: Int,
    var checksum: String,
    var attributes: MutableMap<String, String>,
    var source: String,
    var driverId: String,
    var amount: Double,
    var status: EvChargeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeBatchStatus): Boolean {
        when (status) {
            EvChargeBatchStatus.PLANNED -> return next == EvChargeBatchStatus.IN_PROGRESS || next == EvChargeBatchStatus.COMPLETED
            EvChargeBatchStatus.SCHEDULED -> {
                when (next) {
                    EvChargeBatchStatus.IN_PROGRESS -> return true
                }
                return false
            }
            EvChargeBatchStatus.IN_PROGRESS -> return next == EvChargeBatchStatus.CANCELLED
            EvChargeBatchStatus.COMPLETED -> return next == EvChargeBatchStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: EvChargeBatchStatus) {
        if (!canMoveTo(next)) {
            error("ev charge batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((amount - priority.toDouble()) * 100) / 100

    fun snapshot(): EvChargeBatch {
        return EvChargeBatch(
            code = code,
            name = name,
            ownerId = ownerId,
            region = region,
            priority = priority,
            checksum = checksum,
            attributes = attributes,
            source = source,
            driverId = driverId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a ev charge batch in status planned. */
fun newEvChargeBatch(code: String, name: String): EvChargeBatch {
    return EvChargeBatch(
        code = code,
        name = name,
        ownerId = "",
        region = "",
        priority = 0,
        checksum = "",
        attributes = mutableMapOf<String, String>(),
        source = "",
        driverId = "",
        amount = 0.0,
        status = EvChargeBatchStatus.PLANNED,
    )
}
