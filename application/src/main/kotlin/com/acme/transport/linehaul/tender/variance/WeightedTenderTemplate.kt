package com.acme.transport.linehaul.tender.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted tender template is in its life cycle. */
enum class WeightedTenderTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted tender template as the tender team keeps it. */
data class WeightedTenderTemplate(
    var code: String,
    var name: String,
    var capacity: Int,
    var stopCount: Int,
    var attributes: MutableMap<String, String>,
    var phone: String,
    var loadKg: Double,
    var fuelLiters: Double,
    var timezone: String,
    var unit: String,
    var status: WeightedTenderTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTenderTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTenderTemplateStatus): Boolean {
        when (status) {
            WeightedTenderTemplateStatus.NEW -> return next == WeightedTenderTemplateStatus.QUEUED
            WeightedTenderTemplateStatus.QUEUED -> return next == WeightedTenderTemplateStatus.FAILED
            WeightedTenderTemplateStatus.RUNNING -> return next == WeightedTenderTemplateStatus.DONE || next == WeightedTenderTemplateStatus.FAILED
            WeightedTenderTemplateStatus.DONE -> return next == WeightedTenderTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedTenderTemplateStatus) {
        if (!canMoveTo(next)) {
            error("weighted tender template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((loadKg - capacity.toDouble()) * 100) / 100

    fun snapshot(): WeightedTenderTemplate {
        return WeightedTenderTemplate(
            code = code,
            name = name,
            capacity = capacity,
            stopCount = stopCount,
            attributes = attributes,
            phone = phone,
            loadKg = loadKg,
            fuelLiters = fuelLiters,
            timezone = timezone,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted tender template in status new. */
fun newWeightedTenderTemplate(code: String, name: String): WeightedTenderTemplate {
    return WeightedTenderTemplate(
        code = code,
        name = name,
        capacity = 0,
        stopCount = 0,
        attributes = mutableMapOf<String, String>(),
        phone = "",
        loadKg = 0.0,
        fuelLiters = 0.0,
        timezone = "",
        unit = "",
        status = WeightedTenderTemplateStatus.NEW,
    )
}
