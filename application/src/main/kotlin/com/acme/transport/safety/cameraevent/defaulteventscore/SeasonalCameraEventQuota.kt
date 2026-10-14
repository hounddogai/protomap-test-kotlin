package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal camera event quota is in its life cycle. */
enum class SeasonalCameraEventQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a seasonal camera event quota and its current status. */
data class SeasonalCameraEventQuota(
    var code: String,
    var name: String,
    var version: Int,
    var pallets: Int,
    var notes: String,
    var carrier: String,
    var locale: String,
    var driverId: String,
    var score: Double,
    var region: String,
    var status: SeasonalCameraEventQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCameraEventQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCameraEventQuotaStatus): Boolean {
        when (status) {
            SeasonalCameraEventQuotaStatus.PROPOSED -> return next == SeasonalCameraEventQuotaStatus.REJECTED
            SeasonalCameraEventQuotaStatus.APPROVED -> return next == SeasonalCameraEventQuotaStatus.REJECTED || next == SeasonalCameraEventQuotaStatus.WITHDRAWN
            SeasonalCameraEventQuotaStatus.REJECTED -> return next == SeasonalCameraEventQuotaStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SeasonalCameraEventQuotaStatus) {
        if (!canMoveTo(next)) {
            error("seasonal camera event quota $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"seasonal camera event quota"} $code"

    fun weightedValue(): Double = round((pallets.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): SeasonalCameraEventQuota {
        return SeasonalCameraEventQuota(
            code = code,
            name = name,
            version = version,
            pallets = pallets,
            notes = notes,
            carrier = carrier,
            locale = locale,
            driverId = driverId,
            score = score,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal camera event quota in status proposed. */
fun newSeasonalCameraEventQuota(code: String, name: String): SeasonalCameraEventQuota {
    return SeasonalCameraEventQuota(
        code = code,
        name = name,
        version = 0,
        pallets = 0,
        notes = "",
        carrier = "",
        locale = "",
        driverId = "",
        score = 0.0,
        region = "",
        status = SeasonalCameraEventQuotaStatus.PROPOSED,
    )
}
