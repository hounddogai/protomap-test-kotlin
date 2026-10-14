package com.acme.transport.network.hub.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub template is in its life cycle. */
enum class HubTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One hub template, with what the planners need to act on it. */
data class HubTemplate(
    var code: String,
    var title: String,
    var startsAt: Int,
    var attributes: MutableMap<String, String>,
    var limit: Int,
    var ratio: Double,
    var margin: Double,
    var vehicleId: String,
    var notes: String,
    var status: HubTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubTemplateStatus): Boolean {
        when (status) {
            HubTemplateStatus.REQUESTED -> return next == HubTemplateStatus.EXPIRED
            HubTemplateStatus.GRANTED -> return next == HubTemplateStatus.EXPIRED
            HubTemplateStatus.REVOKED -> return next == HubTemplateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: HubTemplateStatus) {
        if (!canMoveTo(next)) {
            error("hub template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"hub template"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((margin + startsAt.toDouble()) * 100) / 100

    fun snapshot(): HubTemplate {
        return HubTemplate(
            code = code,
            title = title,
            startsAt = startsAt,
            attributes = attributes,
            limit = limit,
            ratio = ratio,
            margin = margin,
            vehicleId = vehicleId,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hub template in status requested. */
fun newHubTemplate(code: String, title: String): HubTemplate {
    return HubTemplate(
        code = code,
        title = title,
        startsAt = 0,
        attributes = mutableMapOf<String, String>(),
        limit = 0,
        ratio = 0.0,
        margin = 0.0,
        vehicleId = "",
        notes = "",
        status = HubTemplateStatus.REQUESTED,
    )
}
