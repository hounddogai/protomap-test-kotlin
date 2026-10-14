package com.acme.transport.customs.hold.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive hold template is in its life cycle. */
enum class AdaptiveHoldTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive hold template. Its code is unique within a tenant. */
data class AdaptiveHoldTemplate(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var active: Boolean,
    var durationMinutes: Int,
    var cost: Double,
    var quantity: Int,
    var startsAt: Int,
    var trailerId: String,
    var status: AdaptiveHoldTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveHoldTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveHoldTemplateStatus): Boolean {
        when (status) {
            AdaptiveHoldTemplateStatus.NEW -> return next == AdaptiveHoldTemplateStatus.DONE
            AdaptiveHoldTemplateStatus.QUEUED -> return next == AdaptiveHoldTemplateStatus.DONE
            AdaptiveHoldTemplateStatus.RUNNING -> return next == AdaptiveHoldTemplateStatus.DONE || next == AdaptiveHoldTemplateStatus.FAILED
            AdaptiveHoldTemplateStatus.DONE -> return next == AdaptiveHoldTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveHoldTemplateStatus) {
        if (!canMoveTo(next)) {
            error("adaptive hold template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netValue(): Double = round(cost * distanceKm * 100) / 100

    fun snapshot(): AdaptiveHoldTemplate {
        return AdaptiveHoldTemplate(
            code = code,
            title = title,
            distanceKm = distanceKm,
            active = active,
            durationMinutes = durationMinutes,
            cost = cost,
            quantity = quantity,
            startsAt = startsAt,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive hold template in status new. */
fun newAdaptiveHoldTemplate(code: String, title: String): AdaptiveHoldTemplate {
    return AdaptiveHoldTemplate(
        code = code,
        title = title,
        distanceKm = 0.0,
        active = false,
        durationMinutes = 0,
        cost = 0.0,
        quantity = 0,
        startsAt = 0,
        trailerId = "",
        status = AdaptiveHoldTemplateStatus.NEW,
    )
}
