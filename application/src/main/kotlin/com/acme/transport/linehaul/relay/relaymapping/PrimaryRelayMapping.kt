package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary relay mapping records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a primary relay mapping is in its life cycle. */
enum class PrimaryRelayMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One primary relay mapping, with what the planners need to act on it. */
data class PrimaryRelayMapping(
    var code: String,
    var title: String,
    var locale: String,
    var distanceKm: Double,
    var revision: Int,
    var approved: Boolean,
    var shiftCode: String,
    var trailerId: String,
    var updatedAt: Int,
    var phone: String,
    var status: PrimaryRelayMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRelayMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRelayMappingStatus): Boolean {
        when (status) {
            PrimaryRelayMappingStatus.IDLE -> return next == PrimaryRelayMappingStatus.DRAINING
            PrimaryRelayMappingStatus.WARMING -> return next == PrimaryRelayMappingStatus.READY || next == PrimaryRelayMappingStatus.STOPPED
            PrimaryRelayMappingStatus.READY -> return next == PrimaryRelayMappingStatus.DRAINING || next == PrimaryRelayMappingStatus.STOPPED
            PrimaryRelayMappingStatus.DRAINING -> {
                when (next) {
                    PrimaryRelayMappingStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRelayMappingStatus) {
        if (!canMoveTo(next)) {
            error("primary relay mapping $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round(updatedAt.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): PrimaryRelayMapping {
        return PrimaryRelayMapping(
            code = code,
            title = title,
            locale = locale,
            distanceKm = distanceKm,
            revision = revision,
            approved = approved,
            shiftCode = shiftCode,
            trailerId = trailerId,
            updatedAt = updatedAt,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary relay mapping in status idle. */
fun newPrimaryRelayMapping(code: String, title: String): PrimaryRelayMapping {
    return PrimaryRelayMapping(
        code = code,
        title = title,
        locale = "",
        distanceKm = 0.0,
        revision = 0,
        approved = false,
        shiftCode = "",
        trailerId = "",
        updatedAt = 0,
        phone = "",
        status = PrimaryRelayMappingStatus.IDLE,
    )
}
