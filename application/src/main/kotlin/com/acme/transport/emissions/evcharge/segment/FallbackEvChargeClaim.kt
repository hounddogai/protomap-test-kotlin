package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback ev charge claim is in its life cycle. */
enum class FallbackEvChargeClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback ev charge claim as the ev charge team keeps it. */
data class FallbackEvChargeClaim(
    var code: String,
    var title: String,
    var externalRef: String,
    var limit: Int,
    var createdAt: Int,
    var pallets: Int,
    var attributes: MutableMap<String, String>,
    var status: FallbackEvChargeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEvChargeClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEvChargeClaimStatus): Boolean {
        when (status) {
            FallbackEvChargeClaimStatus.IDLE -> {
                when (next) {
                    FallbackEvChargeClaimStatus.READY -> return true
                }
                return false
            }
            FallbackEvChargeClaimStatus.WARMING -> return next == FallbackEvChargeClaimStatus.READY || next == FallbackEvChargeClaimStatus.STOPPED
            FallbackEvChargeClaimStatus.READY -> return next == FallbackEvChargeClaimStatus.DRAINING || next == FallbackEvChargeClaimStatus.STOPPED
            FallbackEvChargeClaimStatus.DRAINING -> {
                when (next) {
                    FallbackEvChargeClaimStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackEvChargeClaimStatus) {
        if (!canMoveTo(next)) {
            error("fallback ev charge claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round(pallets.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): FallbackEvChargeClaim {
        return FallbackEvChargeClaim(
            code = code,
            title = title,
            externalRef = externalRef,
            limit = limit,
            createdAt = createdAt,
            pallets = pallets,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback ev charge claim in status idle. */
fun newFallbackEvChargeClaim(code: String, title: String): FallbackEvChargeClaim {
    return FallbackEvChargeClaim(
        code = code,
        title = title,
        externalRef = "",
        limit = 0,
        createdAt = 0,
        pallets = 0,
        attributes = mutableMapOf<String, String>(),
        status = FallbackEvChargeClaimStatus.IDLE,
    )
}
