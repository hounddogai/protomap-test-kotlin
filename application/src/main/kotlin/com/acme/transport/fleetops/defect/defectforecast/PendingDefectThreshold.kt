package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending defect threshold is in its life cycle. */
enum class PendingDefectThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending defect threshold, with what the planners need to act on it. */
data class PendingDefectThreshold(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var name: String,
    var country: String,
    var pinned: Boolean,
    var revision: Int,
    var version: Int,
    var status: PendingDefectThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDefectThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDefectThresholdStatus): Boolean {
        when (status) {
            PendingDefectThresholdStatus.REQUESTED -> {
                when (next) {
                    PendingDefectThresholdStatus.GRANTED, PendingDefectThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            PendingDefectThresholdStatus.GRANTED -> return next == PendingDefectThresholdStatus.EXPIRED
            PendingDefectThresholdStatus.REVOKED -> return next == PendingDefectThresholdStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingDefectThresholdStatus) {
        if (!canMoveTo(next)) {
            error("pending defect threshold $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"pending defect threshold"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round(revision.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): PendingDefectThreshold {
        return PendingDefectThreshold(
            code = code,
            label = label,
            attributes = attributes,
            name = name,
            country = country,
            pinned = pinned,
            revision = revision,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending defect threshold in status requested. */
fun newPendingDefectThreshold(code: String, label: String): PendingDefectThreshold {
    return PendingDefectThreshold(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        name = "",
        country = "",
        pinned = false,
        revision = 0,
        version = 0,
        status = PendingDefectThresholdStatus.REQUESTED,
    )
}
