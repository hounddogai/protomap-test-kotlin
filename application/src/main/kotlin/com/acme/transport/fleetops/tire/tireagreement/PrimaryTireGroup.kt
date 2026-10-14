package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary tire group is in its life cycle. */
enum class PrimaryTireGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary tire group. Its code is unique within a tenant. */
data class PrimaryTireGroup(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var sequence: Int,
    var margin: Double,
    var driverId: String,
    var attempts: Int,
    var phone: String,
    var retries: Int,
    var minimum: Double,
    var status: PrimaryTireGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTireGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTireGroupStatus): Boolean {
        when (status) {
            PrimaryTireGroupStatus.PROPOSED -> return next == PrimaryTireGroupStatus.APPROVED || next == PrimaryTireGroupStatus.REJECTED
            PrimaryTireGroupStatus.APPROVED -> return next == PrimaryTireGroupStatus.REJECTED || next == PrimaryTireGroupStatus.WITHDRAWN
            PrimaryTireGroupStatus.REJECTED -> {
                when (next) {
                    PrimaryTireGroupStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTireGroupStatus) {
        if (!canMoveTo(next)) {
            error("primary tire group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary tire group"} $code"

    fun weightedLoad(): Double = round(minimum * attempts.toDouble() * 100) / 100

    fun snapshot(): PrimaryTireGroup {
        return PrimaryTireGroup(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            sequence = sequence,
            margin = margin,
            driverId = driverId,
            attempts = attempts,
            phone = phone,
            retries = retries,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary tire group in status proposed. */
fun newPrimaryTireGroup(code: String, name: String): PrimaryTireGroup {
    return PrimaryTireGroup(
        code = code,
        name = name,
        durationMinutes = 0,
        sequence = 0,
        margin = 0.0,
        driverId = "",
        attempts = 0,
        phone = "",
        retries = 0,
        minimum = 0.0,
        status = PrimaryTireGroupStatus.PROPOSED,
    )
}
