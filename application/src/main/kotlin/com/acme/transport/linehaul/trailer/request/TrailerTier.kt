package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer tier is in its life cycle. */
enum class TrailerTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A trailer tier as the trailer team keeps it. */
data class TrailerTier(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var attempts: Int,
    var createdAt: Int,
    var timezone: String,
    var retries: Int,
    var durationMinutes: Int,
    var ownerId: String,
    var checksum: String,
    var status: TrailerTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerTierStatus): Boolean {
        when (status) {
            TrailerTierStatus.IDLE -> {
                when (next) {
                    TrailerTierStatus.DRAINING, TrailerTierStatus.STOPPED -> return true
                }
                return false
            }
            TrailerTierStatus.WARMING -> {
                when (next) {
                    TrailerTierStatus.READY, TrailerTierStatus.STOPPED -> return true
                }
                return false
            }
            TrailerTierStatus.READY -> {
                when (next) {
                    TrailerTierStatus.STOPPED -> return true
                }
                return false
            }
            TrailerTierStatus.DRAINING -> return next == TrailerTierStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TrailerTierStatus) {
        if (!canMoveTo(next)) {
            error("trailer tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((retries.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): TrailerTier {
        return TrailerTier(
            code = code,
            label = label,
            attributes = attributes,
            attempts = attempts,
            createdAt = createdAt,
            timezone = timezone,
            retries = retries,
            durationMinutes = durationMinutes,
            ownerId = ownerId,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer tier in status idle. */
fun newTrailerTier(code: String, label: String): TrailerTier {
    return TrailerTier(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        attempts = 0,
        createdAt = 0,
        timezone = "",
        retries = 0,
        durationMinutes = 0,
        ownerId = "",
        checksum = "",
        status = TrailerTierStatus.IDLE,
    )
}
