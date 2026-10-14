package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling mapping is in its life cycle. */
enum class IdlingMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A idling mapping. Its code is unique within a tenant. */
data class IdlingMapping(
    var code: String,
    var title: String,
    var quantity: Int,
    var attributes: MutableMap<String, String>,
    var loadKg: Double,
    var region: String,
    var lane: String,
    var etaMinutes: Int,
    var status: IdlingMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingMappingStatus): Boolean {
        when (status) {
            IdlingMappingStatus.REQUESTED -> {
                when (next) {
                    IdlingMappingStatus.GRANTED, IdlingMappingStatus.EXPIRED -> return true
                }
                return false
            }
            IdlingMappingStatus.GRANTED -> return next == IdlingMappingStatus.REVOKED
            IdlingMappingStatus.REVOKED -> {
                when (next) {
                    IdlingMappingStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingMappingStatus) {
        if (!canMoveTo(next)) {
            error("idling mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((quantity.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): IdlingMapping {
        return IdlingMapping(
            code = code,
            title = title,
            quantity = quantity,
            attributes = attributes,
            loadKg = loadKg,
            region = region,
            lane = lane,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a idling mapping in status requested. */
fun newIdlingMapping(code: String, title: String): IdlingMapping {
    return IdlingMapping(
        code = code,
        title = title,
        quantity = 0,
        attributes = mutableMapOf<String, String>(),
        loadKg = 0.0,
        region = "",
        lane = "",
        etaMinutes = 0,
        status = IdlingMappingStatus.REQUESTED,
    )
}
