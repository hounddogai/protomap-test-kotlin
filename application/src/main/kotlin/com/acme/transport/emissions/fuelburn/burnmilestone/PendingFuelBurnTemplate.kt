package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending fuel burn template is in its life cycle. */
enum class PendingFuelBurnTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending fuel burn template as the fuel burn team keeps it. */
data class PendingFuelBurnTemplate(
    var code: String,
    var title: String,
    var endsAt: Int,
    var graceDays: Int,
    var maximum: Double,
    var stopCount: Int,
    var country: String,
    var capacity: Int,
    var archived: Boolean,
    var status: PendingFuelBurnTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFuelBurnTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFuelBurnTemplateStatus): Boolean {
        when (status) {
            PendingFuelBurnTemplateStatus.OPEN -> {
                when (next) {
                    PendingFuelBurnTemplateStatus.IN_REVIEW -> return true
                }
                return false
            }
            PendingFuelBurnTemplateStatus.IN_REVIEW -> return next == PendingFuelBurnTemplateStatus.RESOLVED
            PendingFuelBurnTemplateStatus.RESOLVED -> {
                when (next) {
                    PendingFuelBurnTemplateStatus.REOPENED, PendingFuelBurnTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingFuelBurnTemplateStatus.REOPENED -> return next == PendingFuelBurnTemplateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingFuelBurnTemplateStatus) {
        if (!canMoveTo(next)) {
            error("pending fuel burn template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netValue(): Double = round((endsAt.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): PendingFuelBurnTemplate {
        return PendingFuelBurnTemplate(
            code = code,
            title = title,
            endsAt = endsAt,
            graceDays = graceDays,
            maximum = maximum,
            stopCount = stopCount,
            country = country,
            capacity = capacity,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending fuel burn template in status open. */
fun newPendingFuelBurnTemplate(code: String, title: String): PendingFuelBurnTemplate {
    return PendingFuelBurnTemplate(
        code = code,
        title = title,
        endsAt = 0,
        graceDays = 0,
        maximum = 0.0,
        stopCount = 0,
        country = "",
        capacity = 0,
        archived = false,
        status = PendingFuelBurnTemplateStatus.OPEN,
    )
}
