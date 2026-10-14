package com.acme.transport.network.zone.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone manifest is in its life cycle. */
enum class ZoneManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a zone manifest and its current status. */
data class ZoneManifest(
    var code: String,
    var name: String,
    var pallets: Int,
    var contactEmail: String,
    var trailerId: String,
    var pinned: Boolean,
    var sequence: Int,
    var revision: Int,
    var floorValue: Double,
    var status: ZoneManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneManifestStatus): Boolean {
        when (status) {
            ZoneManifestStatus.IDLE -> return next == ZoneManifestStatus.WARMING || next == ZoneManifestStatus.STOPPED
            ZoneManifestStatus.WARMING -> return next == ZoneManifestStatus.DRAINING
            ZoneManifestStatus.READY -> {
                when (next) {
                    ZoneManifestStatus.DRAINING -> return true
                }
                return false
            }
            ZoneManifestStatus.DRAINING -> {
                when (next) {
                    ZoneManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneManifestStatus) {
        if (!canMoveTo(next)) {
            error("zone manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"zone manifest"} $code"

    fun weightedValue(): Double = round((pallets.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): ZoneManifest {
        return ZoneManifest(
            code = code,
            name = name,
            pallets = pallets,
            contactEmail = contactEmail,
            trailerId = trailerId,
            pinned = pinned,
            sequence = sequence,
            revision = revision,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a zone manifest in status idle. */
fun newZoneManifest(code: String, name: String): ZoneManifest {
    return ZoneManifest(
        code = code,
        name = name,
        pallets = 0,
        contactEmail = "",
        trailerId = "",
        pinned = false,
        sequence = 0,
        revision = 0,
        floorValue = 0.0,
        status = ZoneManifestStatus.IDLE,
    )
}
