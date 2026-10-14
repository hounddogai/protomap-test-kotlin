package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time group is in its life cycle. */
enum class TransitTimeGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One transit time group, with what the planners need to act on it. */
data class TransitTimeGroup(
    var code: String,
    var name: String,
    var trailerId: String,
    var expiresAt: Int,
    var etaMinutes: Int,
    var attributes: MutableMap<String, String>,
    var status: TransitTimeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeGroupStatus): Boolean {
        when (status) {
            TransitTimeGroupStatus.IDLE -> return next == TransitTimeGroupStatus.WARMING || next == TransitTimeGroupStatus.DRAINING
            TransitTimeGroupStatus.WARMING -> return next == TransitTimeGroupStatus.READY || next == TransitTimeGroupStatus.STOPPED
            TransitTimeGroupStatus.READY -> return next == TransitTimeGroupStatus.DRAINING || next == TransitTimeGroupStatus.STOPPED
            TransitTimeGroupStatus.DRAINING -> {
                when (next) {
                    TransitTimeGroupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeGroupStatus) {
        if (!canMoveTo(next)) {
            error("transit time group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round((expiresAt.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeGroup {
        return TransitTimeGroup(
            code = code,
            name = name,
            trailerId = trailerId,
            expiresAt = expiresAt,
            etaMinutes = etaMinutes,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a transit time group in status idle. */
fun newTransitTimeGroup(code: String, name: String): TransitTimeGroup {
    return TransitTimeGroup(
        code = code,
        name = name,
        trailerId = "",
        expiresAt = 0,
        etaMinutes = 0,
        attributes = mutableMapOf<String, String>(),
        status = TransitTimeGroupStatus.IDLE,
    )
}
