package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tire profile records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire profile is in its life cycle. */
enum class TireProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A tire profile as the tire team keeps it. */
data class TireProfile(
    var code: String,
    var name: String,
    var margin: Double,
    var revision: Int,
    var description: String,
    var status: TireProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireProfileStatus): Boolean {
        when (status) {
            TireProfileStatus.DRAFT -> return next == TireProfileStatus.PENDING || next == TireProfileStatus.SUSPENDED
            TireProfileStatus.PENDING -> return next == TireProfileStatus.SUSPENDED
            TireProfileStatus.ACTIVE -> return next == TireProfileStatus.CLOSED
            TireProfileStatus.SUSPENDED -> {
                when (next) {
                    TireProfileStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireProfileStatus) {
        if (!canMoveTo(next)) {
            error("tire profile $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun netExposure(): Double = round((revision.toDouble() - margin) * 100) / 100

    fun snapshot(): TireProfile {
        return TireProfile(
            code = code,
            name = name,
            margin = margin,
            revision = revision,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tire profile in status draft. */
fun newTireProfile(code: String, name: String): TireProfile {
    return TireProfile(
        code = code,
        name = name,
        margin = 0.0,
        revision = 0,
        description = "",
        status = TireProfileStatus.DRAFT,
    )
}
