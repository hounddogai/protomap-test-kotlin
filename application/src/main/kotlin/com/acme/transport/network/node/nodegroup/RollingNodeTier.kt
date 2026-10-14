package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling node tier is in its life cycle. */
enum class RollingNodeTierStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling node tier. Its code is unique within a tenant. */
data class RollingNodeTier(
    var code: String,
    var name: String,
    var contactEmail: String,
    var attempts: Int,
    var limit: Int,
    var carrier: String,
    var durationMinutes: Int,
    var pinned: Boolean,
    var segment: String,
    var channel: String,
    var phone: String,
    var status: RollingNodeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingNodeTierStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingNodeTierStatus): Boolean {
        when (status) {
            RollingNodeTierStatus.DRAFT -> {
                when (next) {
                    RollingNodeTierStatus.ACTIVE -> return true
                }
                return false
            }
            RollingNodeTierStatus.PENDING -> {
                when (next) {
                    RollingNodeTierStatus.SUSPENDED, RollingNodeTierStatus.CLOSED -> return true
                }
                return false
            }
            RollingNodeTierStatus.ACTIVE -> return next == RollingNodeTierStatus.SUSPENDED
            RollingNodeTierStatus.SUSPENDED -> {
                when (next) {
                    RollingNodeTierStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingNodeTierStatus) {
        if (!canMoveTo(next)) {
            error("rolling node tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling node tier"} $code"

    fun netLoad(): Double = round((attempts.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): RollingNodeTier {
        return RollingNodeTier(
            code = code,
            name = name,
            contactEmail = contactEmail,
            attempts = attempts,
            limit = limit,
            carrier = carrier,
            durationMinutes = durationMinutes,
            pinned = pinned,
            segment = segment,
            channel = channel,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a rolling node tier in status draft. */
fun newRollingNodeTier(code: String, name: String): RollingNodeTier {
    return RollingNodeTier(
        code = code,
        name = name,
        contactEmail = "",
        attempts = 0,
        limit = 0,
        carrier = "",
        durationMinutes = 0,
        pinned = false,
        segment = "",
        channel = "",
        phone = "",
        status = RollingNodeTierStatus.DRAFT,
    )
}
