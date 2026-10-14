package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker claim is in its life cycle. */
enum class LockerClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A locker claim as the locker team keeps it. */
data class LockerClaim(
    var code: String,
    var label: String,
    var graceDays: Int,
    var endsAt: Int,
    var windowDays: Int,
    var attributes: MutableMap<String, String>,
    var archived: Boolean,
    var name: String,
    var discount: Double,
    var expiresAt: Int,
    var attempts: Int,
    var status: LockerClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerClaimStatus): Boolean {
        when (status) {
            LockerClaimStatus.REQUESTED -> return next == LockerClaimStatus.REVOKED || next == LockerClaimStatus.EXPIRED
            LockerClaimStatus.GRANTED -> return next == LockerClaimStatus.EXPIRED
            LockerClaimStatus.REVOKED -> return next == LockerClaimStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LockerClaimStatus) {
        if (!canMoveTo(next)) {
            error("locker claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round((endsAt.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): LockerClaim {
        return LockerClaim(
            code = code,
            label = label,
            graceDays = graceDays,
            endsAt = endsAt,
            windowDays = windowDays,
            attributes = attributes,
            archived = archived,
            name = name,
            discount = discount,
            expiresAt = expiresAt,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a locker claim in status requested. */
fun newLockerClaim(code: String, label: String): LockerClaim {
    return LockerClaim(
        code = code,
        label = label,
        graceDays = 0,
        endsAt = 0,
        windowDays = 0,
        attributes = mutableMapOf<String, String>(),
        archived = false,
        name = "",
        discount = 0.0,
        expiresAt = 0,
        attempts = 0,
        status = LockerClaimStatus.REQUESTED,
    )
}
