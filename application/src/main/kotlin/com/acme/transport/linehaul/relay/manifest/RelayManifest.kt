package com.acme.transport.linehaul.relay.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay manifest records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay manifest is in its life cycle. */
enum class RelayManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A relay manifest. Its code is unique within a tenant. */
data class RelayManifest(
    var code: String,
    var label: String,
    var active: Boolean,
    var limit: Int,
    var loadKg: Double,
    var priority: Int,
    var trailerId: String,
    var contactEmail: String,
    var status: RelayManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayManifestStatus): Boolean {
        when (status) {
            RelayManifestStatus.IDLE -> return next == RelayManifestStatus.READY
            RelayManifestStatus.WARMING -> return next == RelayManifestStatus.READY
            RelayManifestStatus.READY -> return next == RelayManifestStatus.DRAINING || next == RelayManifestStatus.STOPPED
            RelayManifestStatus.DRAINING -> return next == RelayManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RelayManifestStatus) {
        if (!canMoveTo(next)) {
            error("relay manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveValue(): Double = round((limit.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): RelayManifest {
        return RelayManifest(
            code = code,
            label = label,
            active = active,
            limit = limit,
            loadKg = loadKg,
            priority = priority,
            trailerId = trailerId,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a relay manifest in status idle. */
fun newRelayManifest(code: String, label: String): RelayManifest {
    return RelayManifest(
        code = code,
        label = label,
        active = false,
        limit = 0,
        loadKg = 0.0,
        priority = 0,
        trailerId = "",
        contactEmail = "",
        status = RelayManifestStatus.IDLE,
    )
}
