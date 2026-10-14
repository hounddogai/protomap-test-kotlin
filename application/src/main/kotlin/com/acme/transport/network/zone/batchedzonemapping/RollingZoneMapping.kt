package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling zone mapping records of network's zone work, and the rules, stores, and calculations around
// them.

/** Where a rolling zone mapping is in its life cycle. */
enum class RollingZoneMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a rolling zone mapping and its current status. */
data class RollingZoneMapping(
    var code: String,
    var label: String,
    var batchSize: Int,
    var capacity: Int,
    var maximum: Double,
    var startsAt: Int,
    var externalRef: String,
    var name: String,
    var shiftCode: String,
    var archived: Boolean,
    var status: RollingZoneMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingZoneMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingZoneMappingStatus): Boolean {
        when (status) {
            RollingZoneMappingStatus.NEW -> {
                when (next) {
                    RollingZoneMappingStatus.DONE, RollingZoneMappingStatus.FAILED -> return true
                }
                return false
            }
            RollingZoneMappingStatus.QUEUED -> return next == RollingZoneMappingStatus.FAILED
            RollingZoneMappingStatus.RUNNING -> return next == RollingZoneMappingStatus.DONE
            RollingZoneMappingStatus.DONE -> {
                when (next) {
                    RollingZoneMappingStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingZoneMappingStatus) {
        if (!canMoveTo(next)) {
            error("rolling zone mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveTotal(): Double = round(batchSize.toDouble() * maximum * 100) / 100

    fun snapshot(): RollingZoneMapping {
        return RollingZoneMapping(
            code = code,
            label = label,
            batchSize = batchSize,
            capacity = capacity,
            maximum = maximum,
            startsAt = startsAt,
            externalRef = externalRef,
            name = name,
            shiftCode = shiftCode,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling zone mapping in status new. */
fun newRollingZoneMapping(code: String, label: String): RollingZoneMapping {
    return RollingZoneMapping(
        code = code,
        label = label,
        batchSize = 0,
        capacity = 0,
        maximum = 0.0,
        startsAt = 0,
        externalRef = "",
        name = "",
        shiftCode = "",
        archived = false,
        status = RollingZoneMappingStatus.NEW,
    )
}
