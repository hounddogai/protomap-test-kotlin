package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scorecard profile records of safety's scorecard work, and the rules, stores, and calculations around
// them.

/** Where a scorecard profile is in its life cycle. */
enum class ScorecardProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scorecard profile. Its code is unique within a tenant. */
data class ScorecardProfile(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var expiresAt: Int,
    var maximum: Double,
    var pallets: Int,
    var phone: String,
    var name: String,
    var status: ScorecardProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardProfileStatus): Boolean {
        when (status) {
            ScorecardProfileStatus.REQUESTED -> return next == ScorecardProfileStatus.GRANTED
            ScorecardProfileStatus.GRANTED -> return next == ScorecardProfileStatus.REVOKED || next == ScorecardProfileStatus.EXPIRED
            ScorecardProfileStatus.REVOKED -> return next == ScorecardProfileStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScorecardProfileStatus) {
        if (!canMoveTo(next)) {
            error("scorecard profile $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"scorecard profile"} $code"

    fun effectiveValue(): Double = round((pallets.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ScorecardProfile {
        return ScorecardProfile(
            code = code,
            label = label,
            updatedAt = updatedAt,
            expiresAt = expiresAt,
            maximum = maximum,
            pallets = pallets,
            phone = phone,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scorecard profile in status requested. */
fun newScorecardProfile(code: String, label: String): ScorecardProfile {
    return ScorecardProfile(
        code = code,
        label = label,
        updatedAt = 0,
        expiresAt = 0,
        maximum = 0.0,
        pallets = 0,
        phone = "",
        name = "",
        status = ScorecardProfileStatus.REQUESTED,
    )
}
