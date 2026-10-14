package com.acme.transport.freightpay.audit.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal audit adjustment is in its life cycle. */
enum class SeasonalAuditAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A seasonal audit adjustment as the audit team keeps it. */
data class SeasonalAuditAdjustment(
    var code: String,
    var label: String,
    var shiftCode: String,
    var fuelLiters: Double,
    var rate: Double,
    var distanceKm: Double,
    var endsAt: Int,
    var description: String,
    var durationMinutes: Int,
    var revision: Int,
    var ownerId: String,
    var status: SeasonalAuditAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAuditAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAuditAdjustmentStatus): Boolean {
        when (status) {
            SeasonalAuditAdjustmentStatus.CREATED -> {
                when (next) {
                    SeasonalAuditAdjustmentStatus.PUBLISHED, SeasonalAuditAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
            SeasonalAuditAdjustmentStatus.VALIDATED -> {
                when (next) {
                    SeasonalAuditAdjustmentStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalAuditAdjustmentStatus.PUBLISHED -> return next == SeasonalAuditAdjustmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalAuditAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal audit adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedTotal(): Double = round(endsAt.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): SeasonalAuditAdjustment {
        return SeasonalAuditAdjustment(
            code = code,
            label = label,
            shiftCode = shiftCode,
            fuelLiters = fuelLiters,
            rate = rate,
            distanceKm = distanceKm,
            endsAt = endsAt,
            description = description,
            durationMinutes = durationMinutes,
            revision = revision,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal audit adjustment in status created. */
fun newSeasonalAuditAdjustment(code: String, label: String): SeasonalAuditAdjustment {
    return SeasonalAuditAdjustment(
        code = code,
        label = label,
        shiftCode = "",
        fuelLiters = 0.0,
        rate = 0.0,
        distanceKm = 0.0,
        endsAt = 0,
        description = "",
        durationMinutes = 0,
        revision = 0,
        ownerId = "",
        status = SeasonalAuditAdjustmentStatus.CREATED,
    )
}
