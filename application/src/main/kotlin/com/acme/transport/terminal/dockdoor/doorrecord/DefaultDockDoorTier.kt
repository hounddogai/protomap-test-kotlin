package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default dock door tier is in its life cycle. */
enum class DefaultDockDoorTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default dock door tier. Its code is unique within a tenant. */
data class DefaultDockDoorTier(
    var code: String,
    var name: String,
    var sequence: Int,
    var graceDays: Int,
    var ceiling: Double,
    var status: DefaultDockDoorTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDockDoorTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDockDoorTierStatus): Boolean {
        when (status) {
            DefaultDockDoorTierStatus.REQUESTED -> {
                when (next) {
                    DefaultDockDoorTierStatus.REVOKED -> return true
                }
                return false
            }
            DefaultDockDoorTierStatus.GRANTED -> return next == DefaultDockDoorTierStatus.EXPIRED
            DefaultDockDoorTierStatus.REVOKED -> return next == DefaultDockDoorTierStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultDockDoorTierStatus) {
        if (!canMoveTo(next)) {
            error("default dock door tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default dock door tier"} $code"

    fun weightedTotal(): Double = round((ceiling + graceDays.toDouble()) * 100) / 100

    fun snapshot(): DefaultDockDoorTier {
        return DefaultDockDoorTier(
            code = code,
            name = name,
            sequence = sequence,
            graceDays = graceDays,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default dock door tier in status requested. */
fun newDefaultDockDoorTier(code: String, name: String): DefaultDockDoorTier {
    return DefaultDockDoorTier(
        code = code,
        name = name,
        sequence = 0,
        graceDays = 0,
        ceiling = 0.0,
        status = DefaultDockDoorTierStatus.REQUESTED,
    )
}
