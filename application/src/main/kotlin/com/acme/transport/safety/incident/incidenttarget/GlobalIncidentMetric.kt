package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global incident metric is in its life cycle. */
enum class GlobalIncidentMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global incident metric as the incident team keeps it. */
data class GlobalIncidentMetric(
    var code: String,
    var title: String,
    var version: Int,
    var attributes: MutableMap<String, String>,
    var name: String,
    var amount: Double,
    var rate: Double,
    var description: String,
    var status: GlobalIncidentMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalIncidentMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalIncidentMetricStatus): Boolean {
        when (status) {
            GlobalIncidentMetricStatus.CREATED -> return next == GlobalIncidentMetricStatus.RETIRED
            GlobalIncidentMetricStatus.VALIDATED -> {
                when (next) {
                    GlobalIncidentMetricStatus.PUBLISHED -> return true
                }
                return false
            }
            GlobalIncidentMetricStatus.PUBLISHED -> return next == GlobalIncidentMetricStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalIncidentMetricStatus) {
        if (!canMoveTo(next)) {
            error("global incident metric $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((version.toDouble() + rate) * 100) / 100

    fun snapshot(): GlobalIncidentMetric {
        return GlobalIncidentMetric(
            code = code,
            title = title,
            version = version,
            attributes = attributes,
            name = name,
            amount = amount,
            rate = rate,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global incident metric in status created. */
fun newGlobalIncidentMetric(code: String, title: String): GlobalIncidentMetric {
    return GlobalIncidentMetric(
        code = code,
        title = title,
        version = 0,
        attributes = mutableMapOf<String, String>(),
        name = "",
        amount = 0.0,
        rate = 0.0,
        description = "",
        status = GlobalIncidentMetricStatus.CREATED,
    )
}
