package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn template is in its life cycle. */
enum class FuelBurnTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fuel burn template as the fuel burn team keeps it. */
data class FuelBurnTemplate(
    var code: String,
    var name: String,
    var approved: Boolean,
    var startsAt: Int,
    var margin: Double,
    var phone: String,
    var region: String,
    var pinned: Boolean,
    var status: FuelBurnTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnTemplateStatus): Boolean {
        when (status) {
            FuelBurnTemplateStatus.OPEN -> {
                when (next) {
                    FuelBurnTemplateStatus.RESOLVED, FuelBurnTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelBurnTemplateStatus.IN_REVIEW -> return next == FuelBurnTemplateStatus.RESOLVED || next == FuelBurnTemplateStatus.REOPENED
            FuelBurnTemplateStatus.RESOLVED -> return next == FuelBurnTemplateStatus.REOPENED || next == FuelBurnTemplateStatus.ARCHIVED
            FuelBurnTemplateStatus.REOPENED -> {
                when (next) {
                    FuelBurnTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnTemplateStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netVolume(): Double = round(startsAt.toDouble() * margin * 100) / 100

    fun snapshot(): FuelBurnTemplate {
        return FuelBurnTemplate(
            code = code,
            name = name,
            approved = approved,
            startsAt = startsAt,
            margin = margin,
            phone = phone,
            region = region,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fuel burn template in status open. */
fun newFuelBurnTemplate(code: String, name: String): FuelBurnTemplate {
    return FuelBurnTemplate(
        code = code,
        name = name,
        approved = false,
        startsAt = 0,
        margin = 0.0,
        phone = "",
        region = "",
        pinned = false,
        status = FuelBurnTemplateStatus.OPEN,
    )
}
