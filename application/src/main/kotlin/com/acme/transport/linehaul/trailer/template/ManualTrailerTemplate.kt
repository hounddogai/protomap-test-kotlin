package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual trailer template records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a manual trailer template is in its life cycle. */
enum class ManualTrailerTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual trailer template. Its code is unique within a tenant. */
data class ManualTrailerTemplate(
    var code: String,
    var title: String,
    var weight: Double,
    var endsAt: Int,
    var windowDays: Int,
    var fuelLiters: Double,
    var lane: String,
    var floorValue: Double,
    var status: ManualTrailerTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerTemplateStatus): Boolean {
        when (status) {
            ManualTrailerTemplateStatus.DRAFT -> {
                when (next) {
                    ManualTrailerTemplateStatus.PENDING, ManualTrailerTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualTrailerTemplateStatus.PENDING -> return next == ManualTrailerTemplateStatus.ACTIVE
            ManualTrailerTemplateStatus.ACTIVE -> {
                when (next) {
                    ManualTrailerTemplateStatus.CLOSED -> return true
                }
                return false
            }
            ManualTrailerTemplateStatus.SUSPENDED -> {
                when (next) {
                    ManualTrailerTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTrailerTemplateStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round((windowDays.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): ManualTrailerTemplate {
        return ManualTrailerTemplate(
            code = code,
            title = title,
            weight = weight,
            endsAt = endsAt,
            windowDays = windowDays,
            fuelLiters = fuelLiters,
            lane = lane,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual trailer template in status draft. */
fun newManualTrailerTemplate(code: String, title: String): ManualTrailerTemplate {
    return ManualTrailerTemplate(
        code = code,
        title = title,
        weight = 0.0,
        endsAt = 0,
        windowDays = 0,
        fuelLiters = 0.0,
        lane = "",
        floorValue = 0.0,
        status = ManualTrailerTemplateStatus.DRAFT,
    )
}
