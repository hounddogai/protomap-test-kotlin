package com.acme.transport.linehaul.laneplan.plancheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback lane plan checkpoint records of linehaul's lane plan work, and the rules, stores, and
// calculations around them.

/** Where a fallback lane plan checkpoint is in its life cycle. */
enum class FallbackLanePlanCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback lane plan checkpoint. Its code is unique within a tenant. */
data class FallbackLanePlanCheckpoint(
    var code: String,
    var title: String,
    var dock: String,
    var pallets: Int,
    var attributes: MutableMap<String, String>,
    var currency: String,
    var phone: String,
    var vehicleId: String,
    var unit: String,
    var checksum: String,
    var batchSize: Int,
    var status: FallbackLanePlanCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLanePlanCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLanePlanCheckpointStatus): Boolean {
        when (status) {
            FallbackLanePlanCheckpointStatus.PROPOSED -> {
                when (next) {
                    FallbackLanePlanCheckpointStatus.APPROVED, FallbackLanePlanCheckpointStatus.REJECTED -> return true
                }
                return false
            }
            FallbackLanePlanCheckpointStatus.APPROVED -> return next == FallbackLanePlanCheckpointStatus.WITHDRAWN
            FallbackLanePlanCheckpointStatus.REJECTED -> {
                when (next) {
                    FallbackLanePlanCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackLanePlanCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("fallback lane plan checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((pallets.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): FallbackLanePlanCheckpoint {
        return FallbackLanePlanCheckpoint(
            code = code,
            title = title,
            dock = dock,
            pallets = pallets,
            attributes = attributes,
            currency = currency,
            phone = phone,
            vehicleId = vehicleId,
            unit = unit,
            checksum = checksum,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback lane plan checkpoint in status proposed. */
fun newFallbackLanePlanCheckpoint(code: String, title: String): FallbackLanePlanCheckpoint {
    return FallbackLanePlanCheckpoint(
        code = code,
        title = title,
        dock = "",
        pallets = 0,
        attributes = mutableMapOf<String, String>(),
        currency = "",
        phone = "",
        vehicleId = "",
        unit = "",
        checksum = "",
        batchSize = 0,
        status = FallbackLanePlanCheckpointStatus.PROPOSED,
    )
}
