package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending inspection rollup is in its life cycle. */
enum class PendingInspectionRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One pending inspection rollup, with what the planners need to act on it. */
data class PendingInspectionRollup(
    var code: String,
    var label: String,
    var vehicleId: String,
    var attributes: MutableMap<String, String>,
    var retries: Int,
    var trailerId: String,
    var weight: Double,
    var loadKg: Double,
    var status: PendingInspectionRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionRollupStatus): Boolean {
        when (status) {
            PendingInspectionRollupStatus.PROPOSED -> {
                when (next) {
                    PendingInspectionRollupStatus.APPROVED -> return true
                }
                return false
            }
            PendingInspectionRollupStatus.APPROVED -> {
                when (next) {
                    PendingInspectionRollupStatus.REJECTED, PendingInspectionRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingInspectionRollupStatus.REJECTED -> {
                when (next) {
                    PendingInspectionRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingInspectionRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((loadKg + retries.toDouble()) * 100) / 100

    fun snapshot(): PendingInspectionRollup {
        return PendingInspectionRollup(
            code = code,
            label = label,
            vehicleId = vehicleId,
            attributes = attributes,
            retries = retries,
            trailerId = trailerId,
            weight = weight,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending inspection rollup in status proposed. */
fun newPendingInspectionRollup(code: String, label: String): PendingInspectionRollup {
    return PendingInspectionRollup(
        code = code,
        label = label,
        vehicleId = "",
        attributes = mutableMapOf<String, String>(),
        retries = 0,
        trailerId = "",
        weight = 0.0,
        loadKg = 0.0,
        status = PendingInspectionRollupStatus.PROPOSED,
    )
}
