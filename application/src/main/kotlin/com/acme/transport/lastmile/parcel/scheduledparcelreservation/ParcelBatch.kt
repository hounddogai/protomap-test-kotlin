package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel batch is in its life cycle. */
enum class ParcelBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One parcel batch, with what the planners need to act on it. */
data class ParcelBatch(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var sequence: Int,
    var region: String,
    var externalRef: String,
    var expiresAt: Int,
    var etaMinutes: Int,
    var vehicleId: String,
    var visible: Boolean,
    var limit: Int,
    var status: ParcelBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelBatchStatus): Boolean {
        when (status) {
            ParcelBatchStatus.OPEN -> return next == ParcelBatchStatus.IN_REVIEW
            ParcelBatchStatus.IN_REVIEW -> return next == ParcelBatchStatus.REOPENED || next == ParcelBatchStatus.ARCHIVED
            ParcelBatchStatus.RESOLVED -> {
                when (next) {
                    ParcelBatchStatus.REOPENED, ParcelBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            ParcelBatchStatus.REOPENED -> {
                when (next) {
                    ParcelBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelBatchStatus) {
        if (!canMoveTo(next)) {
            error("parcel batch $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"parcel batch"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round(etaMinutes.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): ParcelBatch {
        return ParcelBatch(
            code = code,
            name = name,
            attributes = attributes,
            sequence = sequence,
            region = region,
            externalRef = externalRef,
            expiresAt = expiresAt,
            etaMinutes = etaMinutes,
            vehicleId = vehicleId,
            visible = visible,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a parcel batch in status open. */
fun newParcelBatch(code: String, name: String): ParcelBatch {
    return ParcelBatch(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        sequence = 0,
        region = "",
        externalRef = "",
        expiresAt = 0,
        etaMinutes = 0,
        vehicleId = "",
        visible = false,
        limit = 0,
        status = ParcelBatchStatus.OPEN,
    )
}
