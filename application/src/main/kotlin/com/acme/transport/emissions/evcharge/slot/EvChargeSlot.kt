package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The ev charge slot records of emissions's ev charge work, and the rules, stores, and calculations around
// them.

/** Where a ev charge slot is in its life cycle. */
enum class EvChargeSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A ev charge slot. Its code is unique within a tenant. */
data class EvChargeSlot(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var revision: Int,
    var checksum: String,
    var discount: Double,
    var source: String,
    var etaMinutes: Int,
    var status: EvChargeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeSlotStatus): Boolean {
        when (status) {
            EvChargeSlotStatus.DRAFT -> {
                when (next) {
                    EvChargeSlotStatus.PENDING, EvChargeSlotStatus.ACTIVE -> return true
                }
                return false
            }
            EvChargeSlotStatus.PENDING -> {
                when (next) {
                    EvChargeSlotStatus.ACTIVE -> return true
                }
                return false
            }
            EvChargeSlotStatus.ACTIVE -> {
                when (next) {
                    EvChargeSlotStatus.SUSPENDED -> return true
                }
                return false
            }
            EvChargeSlotStatus.SUSPENDED -> return next == EvChargeSlotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: EvChargeSlotStatus) {
        if (!canMoveTo(next)) {
            error("ev charge slot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun netLoad(): Double = round((expiresAt.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): EvChargeSlot {
        return EvChargeSlot(
            code = code,
            name = name,
            expiresAt = expiresAt,
            revision = revision,
            checksum = checksum,
            discount = discount,
            source = source,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a ev charge slot in status draft. */
fun newEvChargeSlot(code: String, name: String): EvChargeSlot {
    return EvChargeSlot(
        code = code,
        name = name,
        expiresAt = 0,
        revision = 0,
        checksum = "",
        discount = 0.0,
        source = "",
        etaMinutes = 0,
        status = EvChargeSlotStatus.DRAFT,
    )
}
