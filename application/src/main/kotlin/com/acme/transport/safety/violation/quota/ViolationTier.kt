package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation tier is in its life cycle. */
enum class ViolationTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A violation tier. Its code is unique within a tenant. */
data class ViolationTier(
    var code: String,
    var title: String,
    var floorValue: Double,
    var contactEmail: String,
    var minimum: Double,
    var batchSize: Int,
    var createdAt: Int,
    var sequence: Int,
    var expiresAt: Int,
    var durationMinutes: Int,
    var description: String,
    var status: ViolationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationTierStatus): Boolean {
        when (status) {
            ViolationTierStatus.REQUESTED -> return next == ViolationTierStatus.GRANTED
            ViolationTierStatus.GRANTED -> {
                when (next) {
                    ViolationTierStatus.REVOKED -> return true
                }
                return false
            }
            ViolationTierStatus.REVOKED -> return next == ViolationTierStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ViolationTierStatus) {
        if (!canMoveTo(next)) {
            error("violation tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"violation tier"} $code"

    fun effectiveExposure(): Double = round((expiresAt.toDouble() - floorValue) * 100) / 100

    fun snapshot(): ViolationTier {
        return ViolationTier(
            code = code,
            title = title,
            floorValue = floorValue,
            contactEmail = contactEmail,
            minimum = minimum,
            batchSize = batchSize,
            createdAt = createdAt,
            sequence = sequence,
            expiresAt = expiresAt,
            durationMinutes = durationMinutes,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a violation tier in status requested. */
fun newViolationTier(code: String, title: String): ViolationTier {
    return ViolationTier(
        code = code,
        title = title,
        floorValue = 0.0,
        contactEmail = "",
        minimum = 0.0,
        batchSize = 0,
        createdAt = 0,
        sequence = 0,
        expiresAt = 0,
        durationMinutes = 0,
        description = "",
        status = ViolationTierStatus.REQUESTED,
    )
}
