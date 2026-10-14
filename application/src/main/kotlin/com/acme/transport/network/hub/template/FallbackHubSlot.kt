package com.acme.transport.network.hub.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback hub slot is in its life cycle. */
enum class FallbackHubSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback hub slot. Its code is unique within a tenant. */
data class FallbackHubSlot(
    var code: String,
    var name: String,
    var createdAt: Int,
    var pallets: Int,
    var retries: Int,
    var notes: String,
    var version: Int,
    var checksum: String,
    var status: FallbackHubSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackHubSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackHubSlotStatus): Boolean {
        when (status) {
            FallbackHubSlotStatus.NEW -> return next == FallbackHubSlotStatus.DONE || next == FallbackHubSlotStatus.FAILED
            FallbackHubSlotStatus.QUEUED -> return next == FallbackHubSlotStatus.DONE
            FallbackHubSlotStatus.RUNNING -> return next == FallbackHubSlotStatus.DONE || next == FallbackHubSlotStatus.FAILED
            FallbackHubSlotStatus.DONE -> {
                when (next) {
                    FallbackHubSlotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackHubSlotStatus) {
        if (!canMoveTo(next)) {
            error("fallback hub slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"fallback hub slot"} $code"

    fun effectiveLoad(): Double = round(retries.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): FallbackHubSlot {
        return FallbackHubSlot(
            code = code,
            name = name,
            createdAt = createdAt,
            pallets = pallets,
            retries = retries,
            notes = notes,
            version = version,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback hub slot in status new. */
fun newFallbackHubSlot(code: String, name: String): FallbackHubSlot {
    return FallbackHubSlot(
        code = code,
        name = name,
        createdAt = 0,
        pallets = 0,
        retries = 0,
        notes = "",
        version = 0,
        checksum = "",
        status = FallbackHubSlotStatus.NEW,
    )
}
