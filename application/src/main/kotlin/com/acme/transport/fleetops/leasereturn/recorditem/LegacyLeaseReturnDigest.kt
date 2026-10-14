package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy lease return digest is in its life cycle. */
enum class LegacyLeaseReturnDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a legacy lease return digest and its current status. */
data class LegacyLeaseReturnDigest(
    var code: String,
    var name: String,
    var graceDays: Int,
    var dock: String,
    var ratio: Double,
    var endsAt: Int,
    var vehicleId: String,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var cost: Double,
    var pallets: Int,
    var status: LegacyLeaseReturnDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLeaseReturnDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLeaseReturnDigestStatus): Boolean {
        when (status) {
            LegacyLeaseReturnDigestStatus.IDLE -> {
                when (next) {
                    LegacyLeaseReturnDigestStatus.READY -> return true
                }
                return false
            }
            LegacyLeaseReturnDigestStatus.WARMING -> {
                when (next) {
                    LegacyLeaseReturnDigestStatus.READY, LegacyLeaseReturnDigestStatus.DRAINING -> return true
                }
                return false
            }
            LegacyLeaseReturnDigestStatus.READY -> return next == LegacyLeaseReturnDigestStatus.DRAINING || next == LegacyLeaseReturnDigestStatus.STOPPED
            LegacyLeaseReturnDigestStatus.DRAINING -> {
                when (next) {
                    LegacyLeaseReturnDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyLeaseReturnDigestStatus) {
        if (!canMoveTo(next)) {
            error("legacy lease return digest $code cannot move from $status to $next")
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

    fun projectedVolume(): Double = round(cost * endsAt.toDouble() * 100) / 100

    fun snapshot(): LegacyLeaseReturnDigest {
        return LegacyLeaseReturnDigest(
            code = code,
            name = name,
            graceDays = graceDays,
            dock = dock,
            ratio = ratio,
            endsAt = endsAt,
            vehicleId = vehicleId,
            timezone = timezone,
            attributes = attributes,
            cost = cost,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy lease return digest in status idle. */
fun newLegacyLeaseReturnDigest(code: String, name: String): LegacyLeaseReturnDigest {
    return LegacyLeaseReturnDigest(
        code = code,
        name = name,
        graceDays = 0,
        dock = "",
        ratio = 0.0,
        endsAt = 0,
        vehicleId = "",
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        cost = 0.0,
        pallets = 0,
        status = LegacyLeaseReturnDigestStatus.IDLE,
    )
}
