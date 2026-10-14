package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery preference is in its life cycle. */
enum class RedeliveryPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One redelivery preference, with what the planners need to act on it. */
data class RedeliveryPreference(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var pinned: Boolean,
    var retries: Int,
    var attributes: MutableMap<String, String>,
    var discount: Double,
    var trailerId: String,
    var shiftCode: String,
    var ratio: Double,
    var attempts: Int,
    var status: RedeliveryPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryPreferenceStatus): Boolean {
        when (status) {
            RedeliveryPreferenceStatus.IDLE -> return next == RedeliveryPreferenceStatus.WARMING || next == RedeliveryPreferenceStatus.DRAINING
            RedeliveryPreferenceStatus.WARMING -> return next == RedeliveryPreferenceStatus.READY || next == RedeliveryPreferenceStatus.STOPPED
            RedeliveryPreferenceStatus.READY -> return next == RedeliveryPreferenceStatus.DRAINING
            RedeliveryPreferenceStatus.DRAINING -> return next == RedeliveryPreferenceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RedeliveryPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("redelivery preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((attempts.toDouble() + discount) * 100) / 100

    fun snapshot(): RedeliveryPreference {
        return RedeliveryPreference(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            pinned = pinned,
            retries = retries,
            attributes = attributes,
            discount = discount,
            trailerId = trailerId,
            shiftCode = shiftCode,
            ratio = ratio,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a redelivery preference in status idle. */
fun newRedeliveryPreference(code: String, name: String): RedeliveryPreference {
    return RedeliveryPreference(
        code = code,
        name = name,
        fuelLiters = 0.0,
        pinned = false,
        retries = 0,
        attributes = mutableMapOf<String, String>(),
        discount = 0.0,
        trailerId = "",
        shiftCode = "",
        ratio = 0.0,
        attempts = 0,
        status = RedeliveryPreferenceStatus.IDLE,
    )
}
