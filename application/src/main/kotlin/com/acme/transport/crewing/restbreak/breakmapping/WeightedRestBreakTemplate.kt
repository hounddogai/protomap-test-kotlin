package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted rest break template is in its life cycle. */
enum class WeightedRestBreakTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted rest break template. Its code is unique within a tenant. */
data class WeightedRestBreakTemplate(
    var code: String,
    var label: String,
    var channel: String,
    var expiresAt: Int,
    var attributes: MutableMap<String, String>,
    var odometer: Int,
    var pallets: Int,
    var fuelLiters: Double,
    var minimum: Double,
    var status: WeightedRestBreakTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRestBreakTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRestBreakTemplateStatus): Boolean {
        when (status) {
            WeightedRestBreakTemplateStatus.CREATED -> {
                when (next) {
                    WeightedRestBreakTemplateStatus.RETIRED -> return true
                }
                return false
            }
            WeightedRestBreakTemplateStatus.VALIDATED -> return next == WeightedRestBreakTemplateStatus.RETIRED
            WeightedRestBreakTemplateStatus.PUBLISHED -> return next == WeightedRestBreakTemplateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedRestBreakTemplateStatus) {
        if (!canMoveTo(next)) {
            error("weighted rest break template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted rest break template"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round(minimum * pallets.toDouble() * 100) / 100

    fun snapshot(): WeightedRestBreakTemplate {
        return WeightedRestBreakTemplate(
            code = code,
            label = label,
            channel = channel,
            expiresAt = expiresAt,
            attributes = attributes,
            odometer = odometer,
            pallets = pallets,
            fuelLiters = fuelLiters,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted rest break template in status created. */
fun newWeightedRestBreakTemplate(code: String, label: String): WeightedRestBreakTemplate {
    return WeightedRestBreakTemplate(
        code = code,
        label = label,
        channel = "",
        expiresAt = 0,
        attributes = mutableMapOf<String, String>(),
        odometer = 0,
        pallets = 0,
        fuelLiters = 0.0,
        minimum = 0.0,
        status = WeightedRestBreakTemplateStatus.CREATED,
    )
}
