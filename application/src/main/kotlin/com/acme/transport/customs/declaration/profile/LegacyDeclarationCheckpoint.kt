package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy declaration checkpoint is in its life cycle. */
enum class LegacyDeclarationCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A legacy declaration checkpoint as the declaration team keeps it. */
data class LegacyDeclarationCheckpoint(
    var code: String,
    var name: String,
    var trailerId: String,
    var driverId: String,
    var version: Int,
    var ownerId: String,
    var threshold: Double,
    var description: String,
    var status: LegacyDeclarationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDeclarationCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDeclarationCheckpointStatus): Boolean {
        when (status) {
            LegacyDeclarationCheckpointStatus.REQUESTED -> {
                when (next) {
                    LegacyDeclarationCheckpointStatus.REVOKED, LegacyDeclarationCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyDeclarationCheckpointStatus.GRANTED -> {
                when (next) {
                    LegacyDeclarationCheckpointStatus.REVOKED, LegacyDeclarationCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyDeclarationCheckpointStatus.REVOKED -> {
                when (next) {
                    LegacyDeclarationCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDeclarationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("legacy declaration checkpoint $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round((threshold + version.toDouble()) * 100) / 100

    fun snapshot(): LegacyDeclarationCheckpoint {
        return LegacyDeclarationCheckpoint(
            code = code,
            name = name,
            trailerId = trailerId,
            driverId = driverId,
            version = version,
            ownerId = ownerId,
            threshold = threshold,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy declaration checkpoint in status requested. */
fun newLegacyDeclarationCheckpoint(code: String, name: String): LegacyDeclarationCheckpoint {
    return LegacyDeclarationCheckpoint(
        code = code,
        name = name,
        trailerId = "",
        driverId = "",
        version = 0,
        ownerId = "",
        threshold = 0.0,
        description = "",
        status = LegacyDeclarationCheckpointStatus.REQUESTED,
    )
}
