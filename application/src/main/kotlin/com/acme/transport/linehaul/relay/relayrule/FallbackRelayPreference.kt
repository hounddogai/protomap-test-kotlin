package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback relay preference is in its life cycle. */
enum class FallbackRelayPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback relay preference as the relay team keeps it. */
data class FallbackRelayPreference(
    var code: String,
    var name: String,
    var discount: Double,
    var description: String,
    var quantity: Int,
    var createdAt: Int,
    var status: FallbackRelayPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRelayPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRelayPreferenceStatus): Boolean {
        when (status) {
            FallbackRelayPreferenceStatus.IDLE -> return next == FallbackRelayPreferenceStatus.DRAINING
            FallbackRelayPreferenceStatus.WARMING -> return next == FallbackRelayPreferenceStatus.READY || next == FallbackRelayPreferenceStatus.DRAINING
            FallbackRelayPreferenceStatus.READY -> {
                when (next) {
                    FallbackRelayPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            FallbackRelayPreferenceStatus.DRAINING -> return next == FallbackRelayPreferenceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackRelayPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("fallback relay preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedVolume(): Double = round((discount + createdAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackRelayPreference {
        return FallbackRelayPreference(
            code = code,
            name = name,
            discount = discount,
            description = description,
            quantity = quantity,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback relay preference in status idle. */
fun newFallbackRelayPreference(code: String, name: String): FallbackRelayPreference {
    return FallbackRelayPreference(
        code = code,
        name = name,
        discount = 0.0,
        description = "",
        quantity = 0,
        createdAt = 0,
        status = FallbackRelayPreferenceStatus.IDLE,
    )
}
