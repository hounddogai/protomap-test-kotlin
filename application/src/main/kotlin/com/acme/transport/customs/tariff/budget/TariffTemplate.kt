package com.acme.transport.customs.tariff.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff template is in its life cycle. */
enum class TariffTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tariff template as the tariff team keeps it. */
data class TariffTemplate(
    var code: String,
    var name: String,
    var windowDays: Int,
    var distanceKm: Double,
    var category: String,
    var priority: Int,
    var attributes: MutableMap<String, String>,
    var rate: Double,
    var pinned: Boolean,
    var endsAt: Int,
    var status: TariffTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffTemplateStatus): Boolean {
        when (status) {
            TariffTemplateStatus.PLANNED -> return next == TariffTemplateStatus.COMPLETED || next == TariffTemplateStatus.CANCELLED
            TariffTemplateStatus.SCHEDULED -> return next == TariffTemplateStatus.IN_PROGRESS
            TariffTemplateStatus.IN_PROGRESS -> {
                when (next) {
                    TariffTemplateStatus.COMPLETED, TariffTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            TariffTemplateStatus.COMPLETED -> {
                when (next) {
                    TariffTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffTemplateStatus) {
        if (!canMoveTo(next)) {
            error("tariff template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tariff template"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round((priority.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): TariffTemplate {
        return TariffTemplate(
            code = code,
            name = name,
            windowDays = windowDays,
            distanceKm = distanceKm,
            category = category,
            priority = priority,
            attributes = attributes,
            rate = rate,
            pinned = pinned,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tariff template in status planned. */
fun newTariffTemplate(code: String, name: String): TariffTemplate {
    return TariffTemplate(
        code = code,
        name = name,
        windowDays = 0,
        distanceKm = 0.0,
        category = "",
        priority = 0,
        attributes = mutableMapOf<String, String>(),
        rate = 0.0,
        pinned = false,
        endsAt = 0,
        status = TariffTemplateStatus.PLANNED,
    )
}
