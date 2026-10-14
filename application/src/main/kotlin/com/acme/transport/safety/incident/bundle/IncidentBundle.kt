package com.acme.transport.safety.incident.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The incident bundle records of safety's incident work, and the rules, stores, and calculations around them.

/** Where a incident bundle is in its life cycle. */
enum class IncidentBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A incident bundle. Its code is unique within a tenant. */
data class IncidentBundle(
    var code: String,
    var name: String,
    var carrier: String,
    var distanceKm: Double,
    var retries: Int,
    var priority: Int,
    var threshold: Double,
    var locale: String,
    var attributes: MutableMap<String, String>,
    var amount: Double,
    var status: IncidentBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentBundleStatus): Boolean {
        when (status) {
            IncidentBundleStatus.DRAFT -> {
                when (next) {
                    IncidentBundleStatus.PENDING -> return true
                }
                return false
            }
            IncidentBundleStatus.PENDING -> return next == IncidentBundleStatus.ACTIVE
            IncidentBundleStatus.ACTIVE -> return next == IncidentBundleStatus.CLOSED
            IncidentBundleStatus.SUSPENDED -> {
                when (next) {
                    IncidentBundleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentBundleStatus) {
        if (!canMoveTo(next)) {
            error("incident bundle $code cannot move from $status to $next")
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

    fun netLoad(): Double = round(threshold * priority.toDouble() * 100) / 100

    fun snapshot(): IncidentBundle {
        return IncidentBundle(
            code = code,
            name = name,
            carrier = carrier,
            distanceKm = distanceKm,
            retries = retries,
            priority = priority,
            threshold = threshold,
            locale = locale,
            attributes = attributes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a incident bundle in status draft. */
fun newIncidentBundle(code: String, name: String): IncidentBundle {
    return IncidentBundle(
        code = code,
        name = name,
        carrier = "",
        distanceKm = 0.0,
        retries = 0,
        priority = 0,
        threshold = 0.0,
        locale = "",
        attributes = mutableMapOf<String, String>(),
        amount = 0.0,
        status = IncidentBundleStatus.DRAFT,
    )
}
