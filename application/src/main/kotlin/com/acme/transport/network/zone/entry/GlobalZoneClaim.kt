package com.acme.transport.network.zone.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global zone claim is in its life cycle. */
enum class GlobalZoneClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a global zone claim and its current status. */
data class GlobalZoneClaim(
    var code: String,
    var title: String,
    var notes: String,
    var durationMinutes: Int,
    var sequence: Int,
    var rate: Double,
    var archived: Boolean,
    var etaMinutes: Int,
    var source: String,
    var phone: String,
    var version: Int,
    var status: GlobalZoneClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalZoneClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalZoneClaimStatus): Boolean {
        when (status) {
            GlobalZoneClaimStatus.PLANNED -> {
                when (next) {
                    GlobalZoneClaimStatus.SCHEDULED, GlobalZoneClaimStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalZoneClaimStatus.SCHEDULED -> return next == GlobalZoneClaimStatus.COMPLETED || next == GlobalZoneClaimStatus.CANCELLED
            GlobalZoneClaimStatus.IN_PROGRESS -> return next == GlobalZoneClaimStatus.COMPLETED || next == GlobalZoneClaimStatus.CANCELLED
            GlobalZoneClaimStatus.COMPLETED -> {
                when (next) {
                    GlobalZoneClaimStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalZoneClaimStatus) {
        if (!canMoveTo(next)) {
            error("global zone claim $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round((rate + sequence.toDouble()) * 100) / 100

    fun snapshot(): GlobalZoneClaim {
        return GlobalZoneClaim(
            code = code,
            title = title,
            notes = notes,
            durationMinutes = durationMinutes,
            sequence = sequence,
            rate = rate,
            archived = archived,
            etaMinutes = etaMinutes,
            source = source,
            phone = phone,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global zone claim in status planned. */
fun newGlobalZoneClaim(code: String, title: String): GlobalZoneClaim {
    return GlobalZoneClaim(
        code = code,
        title = title,
        notes = "",
        durationMinutes = 0,
        sequence = 0,
        rate = 0.0,
        archived = false,
        etaMinutes = 0,
        source = "",
        phone = "",
        version = 0,
        status = GlobalZoneClaimStatus.PLANNED,
    )
}
