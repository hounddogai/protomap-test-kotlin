package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.text.normalize

/** Where a global zone tier is in its life cycle. */
enum class GlobalZoneTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One global zone tier, with what the planners need to act on it. */
data class GlobalZoneTier(
    var code: String,
    var name: String,
    var ownerId: String,
    var etaMinutes: Int,
    var pinned: Boolean,
    var status: GlobalZoneTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalZoneTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalZoneTierStatus): Boolean {
        when (status) {
            GlobalZoneTierStatus.REQUESTED -> {
                when (next) {
                    GlobalZoneTierStatus.REVOKED -> return true
                }
                return false
            }
            GlobalZoneTierStatus.GRANTED -> return next == GlobalZoneTierStatus.REVOKED || next == GlobalZoneTierStatus.EXPIRED
            GlobalZoneTierStatus.REVOKED -> {
                when (next) {
                    GlobalZoneTierStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalZoneTierStatus) {
        if (!canMoveTo(next)) {
            error("global zone tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): GlobalZoneTier {
        return GlobalZoneTier(
            code = code,
            name = name,
            ownerId = ownerId,
            etaMinutes = etaMinutes,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global zone tier in status requested. */
fun newGlobalZoneTier(code: String, name: String): GlobalZoneTier {
    return GlobalZoneTier(
        code = code,
        name = name,
        ownerId = "",
        etaMinutes = 0,
        pinned = false,
        status = GlobalZoneTierStatus.REQUESTED,
    )
}
