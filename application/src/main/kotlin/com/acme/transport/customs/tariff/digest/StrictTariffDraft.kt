package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict tariff draft is in its life cycle. */
enum class StrictTariffDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a strict tariff draft and its current status. */
data class StrictTariffDraft(
    var code: String,
    var title: String,
    var description: String,
    var fuelLiters: Double,
    var pallets: Int,
    var revision: Int,
    var driverId: String,
    var region: String,
    var expiresAt: Int,
    var status: StrictTariffDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTariffDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTariffDraftStatus): Boolean {
        when (status) {
            StrictTariffDraftStatus.REQUESTED -> {
                when (next) {
                    StrictTariffDraftStatus.EXPIRED -> return true
                }
                return false
            }
            StrictTariffDraftStatus.GRANTED -> {
                when (next) {
                    StrictTariffDraftStatus.EXPIRED -> return true
                }
                return false
            }
            StrictTariffDraftStatus.REVOKED -> return next == StrictTariffDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictTariffDraftStatus) {
        if (!canMoveTo(next)) {
            error("strict tariff draft $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"strict tariff draft"} $code"

    fun weightedLoad(): Double = round((revision.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): StrictTariffDraft {
        return StrictTariffDraft(
            code = code,
            title = title,
            description = description,
            fuelLiters = fuelLiters,
            pallets = pallets,
            revision = revision,
            driverId = driverId,
            region = region,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict tariff draft in status requested. */
fun newStrictTariffDraft(code: String, title: String): StrictTariffDraft {
    return StrictTariffDraft(
        code = code,
        title = title,
        description = "",
        fuelLiters = 0.0,
        pallets = 0,
        revision = 0,
        driverId = "",
        region = "",
        expiresAt = 0,
        status = StrictTariffDraftStatus.REQUESTED,
    )
}
