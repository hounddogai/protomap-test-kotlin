package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub mapping records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub mapping is in its life cycle. */
enum class HubMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A hub mapping. Its code is unique within a tenant. */
data class HubMapping(
    var code: String,
    var name: String,
    var endsAt: Int,
    var locale: String,
    var trailerId: String,
    var maximum: Double,
    var attributes: MutableMap<String, String>,
    var ratio: Double,
    var status: HubMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubMappingStatus): Boolean {
        when (status) {
            HubMappingStatus.CREATED -> {
                when (next) {
                    HubMappingStatus.VALIDATED, HubMappingStatus.RETIRED -> return true
                }
                return false
            }
            HubMappingStatus.VALIDATED -> return next == HubMappingStatus.PUBLISHED || next == HubMappingStatus.RETIRED
            HubMappingStatus.PUBLISHED -> return next == HubMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: HubMappingStatus) {
        if (!canMoveTo(next)) {
            error("hub mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"hub mapping"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round((maximum + endsAt.toDouble()) * 100) / 100

    fun snapshot(): HubMapping {
        return HubMapping(
            code = code,
            name = name,
            endsAt = endsAt,
            locale = locale,
            trailerId = trailerId,
            maximum = maximum,
            attributes = attributes,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a hub mapping in status created. */
fun newHubMapping(code: String, name: String): HubMapping {
    return HubMapping(
        code = code,
        name = name,
        endsAt = 0,
        locale = "",
        trailerId = "",
        maximum = 0.0,
        attributes = mutableMapOf<String, String>(),
        ratio = 0.0,
        status = HubMappingStatus.CREATED,
    )
}
