package com.acme.transport.terminal.dockdoor.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global dock door claim is in its life cycle. */
enum class GlobalDockDoorClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global dock door claim. Its code is unique within a tenant. */
data class GlobalDockDoorClaim(
    var code: String,
    var title: String,
    var locale: String,
    var active: Boolean,
    var notes: String,
    var expiresAt: Int,
    var fuelLiters: Double,
    var margin: Double,
    var sequence: Int,
    var status: GlobalDockDoorClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDockDoorClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDockDoorClaimStatus): Boolean {
        when (status) {
            GlobalDockDoorClaimStatus.DRAFT -> {
                when (next) {
                    GlobalDockDoorClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            GlobalDockDoorClaimStatus.PENDING -> {
                when (next) {
                    GlobalDockDoorClaimStatus.ACTIVE, GlobalDockDoorClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            GlobalDockDoorClaimStatus.ACTIVE -> return next == GlobalDockDoorClaimStatus.CLOSED
            GlobalDockDoorClaimStatus.SUSPENDED -> return next == GlobalDockDoorClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: GlobalDockDoorClaimStatus) {
        if (!canMoveTo(next)) {
            error("global dock door claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global dock door claim"} $code"

    fun netExposure(): Double = round((expiresAt.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): GlobalDockDoorClaim {
        return GlobalDockDoorClaim(
            code = code,
            title = title,
            locale = locale,
            active = active,
            notes = notes,
            expiresAt = expiresAt,
            fuelLiters = fuelLiters,
            margin = margin,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a global dock door claim in status draft. */
fun newGlobalDockDoorClaim(code: String, title: String): GlobalDockDoorClaim {
    return GlobalDockDoorClaim(
        code = code,
        title = title,
        locale = "",
        active = false,
        notes = "",
        expiresAt = 0,
        fuelLiters = 0.0,
        margin = 0.0,
        sequence = 0,
        status = GlobalDockDoorClaimStatus.DRAFT,
    )
}
