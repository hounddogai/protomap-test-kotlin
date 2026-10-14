package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone threshold records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone threshold is in its life cycle. */
enum class ZoneThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A zone threshold. Its code is unique within a tenant. */
data class ZoneThreshold(
    var code: String,
    var label: String,
    var sequence: Int,
    var revision: Int,
    var weight: Double,
    var name: String,
    var status: ZoneThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneThresholdStatus): Boolean {
        when (status) {
            ZoneThresholdStatus.NEW -> {
                when (next) {
                    ZoneThresholdStatus.QUEUED, ZoneThresholdStatus.FAILED -> return true
                }
                return false
            }
            ZoneThresholdStatus.QUEUED -> return next == ZoneThresholdStatus.DONE || next == ZoneThresholdStatus.FAILED
            ZoneThresholdStatus.RUNNING -> {
                when (next) {
                    ZoneThresholdStatus.DONE, ZoneThresholdStatus.FAILED -> return true
                }
                return false
            }
            ZoneThresholdStatus.DONE -> return next == ZoneThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ZoneThresholdStatus) {
        if (!canMoveTo(next)) {
            error("zone threshold $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round((sequence.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): ZoneThreshold {
        return ZoneThreshold(
            code = code,
            label = label,
            sequence = sequence,
            revision = revision,
            weight = weight,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a zone threshold in status new. */
fun newZoneThreshold(code: String, label: String): ZoneThreshold {
    return ZoneThreshold(
        code = code,
        label = label,
        sequence = 0,
        revision = 0,
        weight = 0.0,
        name = "",
        status = ZoneThresholdStatus.NEW,
    )
}
