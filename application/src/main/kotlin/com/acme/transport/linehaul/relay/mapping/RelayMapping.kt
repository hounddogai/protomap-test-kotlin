package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay mapping records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay mapping is in its life cycle. */
enum class RelayMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A relay mapping as the relay team keeps it. */
data class RelayMapping(
    var code: String,
    var label: String,
    var rate: Double,
    var stopCount: Int,
    var revision: Int,
    var loadKg: Double,
    var trailerId: String,
    var endsAt: Int,
    var status: RelayMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayMappingStatus): Boolean {
        when (status) {
            RelayMappingStatus.PLANNED -> return next == RelayMappingStatus.CANCELLED
            RelayMappingStatus.SCHEDULED -> {
                when (next) {
                    RelayMappingStatus.IN_PROGRESS, RelayMappingStatus.CANCELLED -> return true
                }
                return false
            }
            RelayMappingStatus.IN_PROGRESS -> return next == RelayMappingStatus.COMPLETED || next == RelayMappingStatus.CANCELLED
            RelayMappingStatus.COMPLETED -> {
                when (next) {
                    RelayMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayMappingStatus) {
        if (!canMoveTo(next)) {
            error("relay mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun netTotal(): Double = round(rate * endsAt.toDouble() * 100) / 100

    fun snapshot(): RelayMapping {
        return RelayMapping(
            code = code,
            label = label,
            rate = rate,
            stopCount = stopCount,
            revision = revision,
            loadKg = loadKg,
            trailerId = trailerId,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a relay mapping in status planned. */
fun newRelayMapping(code: String, label: String): RelayMapping {
    return RelayMapping(
        code = code,
        label = label,
        rate = 0.0,
        stopCount = 0,
        revision = 0,
        loadKg = 0.0,
        trailerId = "",
        endsAt = 0,
        status = RelayMappingStatus.PLANNED,
    )
}
