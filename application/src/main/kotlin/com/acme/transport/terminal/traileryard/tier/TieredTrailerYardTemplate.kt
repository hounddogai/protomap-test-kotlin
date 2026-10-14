package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered trailer yard template is in its life cycle. */
enum class TieredTrailerYardTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tiered trailer yard template as the trailer yard team keeps it. */
data class TieredTrailerYardTemplate(
    var code: String,
    var label: String,
    var startsAt: Int,
    var odometer: Int,
    var driverId: String,
    var source: String,
    var version: Int,
    var shiftCode: String,
    var status: TieredTrailerYardTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTrailerYardTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTrailerYardTemplateStatus): Boolean {
        when (status) {
            TieredTrailerYardTemplateStatus.PLANNED -> {
                when (next) {
                    TieredTrailerYardTemplateStatus.COMPLETED -> return true
                }
                return false
            }
            TieredTrailerYardTemplateStatus.SCHEDULED -> return next == TieredTrailerYardTemplateStatus.COMPLETED
            TieredTrailerYardTemplateStatus.IN_PROGRESS -> return next == TieredTrailerYardTemplateStatus.COMPLETED || next == TieredTrailerYardTemplateStatus.CANCELLED
            TieredTrailerYardTemplateStatus.COMPLETED -> return next == TieredTrailerYardTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredTrailerYardTemplateStatus) {
        if (!canMoveTo(next)) {
            error("tiered trailer yard template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"tiered trailer yard template"} $code"

    fun weightedTotal(): Double = round((odometer.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): TieredTrailerYardTemplate {
        return TieredTrailerYardTemplate(
            code = code,
            label = label,
            startsAt = startsAt,
            odometer = odometer,
            driverId = driverId,
            source = source,
            version = version,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered trailer yard template in status planned. */
fun newTieredTrailerYardTemplate(code: String, label: String): TieredTrailerYardTemplate {
    return TieredTrailerYardTemplate(
        code = code,
        label = label,
        startsAt = 0,
        odometer = 0,
        driverId = "",
        source = "",
        version = 0,
        shiftCode = "",
        status = TieredTrailerYardTemplateStatus.PLANNED,
    )
}
