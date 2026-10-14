package com.acme.transport.freightpay.audit.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered audit tier records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a tiered audit tier is in its life cycle. */
enum class TieredAuditTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tiered audit tier. Its code is unique within a tenant. */
data class TieredAuditTier(
    var code: String,
    var name: String,
    var segment: String,
    var shiftCode: String,
    var version: Int,
    var minimum: Double,
    var country: String,
    var status: TieredAuditTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAuditTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAuditTierStatus): Boolean {
        when (status) {
            TieredAuditTierStatus.PLANNED -> {
                when (next) {
                    TieredAuditTierStatus.IN_PROGRESS, TieredAuditTierStatus.CANCELLED -> return true
                }
                return false
            }
            TieredAuditTierStatus.SCHEDULED -> {
                when (next) {
                    TieredAuditTierStatus.CANCELLED -> return true
                }
                return false
            }
            TieredAuditTierStatus.IN_PROGRESS -> return next == TieredAuditTierStatus.COMPLETED || next == TieredAuditTierStatus.CANCELLED
            TieredAuditTierStatus.COMPLETED -> {
                when (next) {
                    TieredAuditTierStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredAuditTierStatus) {
        if (!canMoveTo(next)) {
            error("tiered audit tier $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun adjustedLoad(): Double = round(version.toDouble() * minimum * 100) / 100

    fun snapshot(): TieredAuditTier {
        return TieredAuditTier(
            code = code,
            name = name,
            segment = segment,
            shiftCode = shiftCode,
            version = version,
            minimum = minimum,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered audit tier in status planned. */
fun newTieredAuditTier(code: String, name: String): TieredAuditTier {
    return TieredAuditTier(
        code = code,
        name = name,
        segment = "",
        shiftCode = "",
        version = 0,
        minimum = 0.0,
        country = "",
        status = TieredAuditTierStatus.PLANNED,
    )
}
