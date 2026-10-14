package com.acme.transport.crewing.swap.swapclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary swap mapping is in its life cycle. */
enum class PrimarySwapMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a primary swap mapping and its current status. */
data class PrimarySwapMapping(
    var code: String,
    var title: String,
    var carrier: String,
    var score: Double,
    var externalRef: String,
    var phone: String,
    var active: Boolean,
    var maximum: Double,
    var margin: Double,
    var attributes: MutableMap<String, String>,
    var status: PrimarySwapMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimarySwapMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimarySwapMappingStatus): Boolean {
        when (status) {
            PrimarySwapMappingStatus.OPEN -> return next == PrimarySwapMappingStatus.ARCHIVED
            PrimarySwapMappingStatus.IN_REVIEW -> {
                when (next) {
                    PrimarySwapMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimarySwapMappingStatus.RESOLVED -> return next == PrimarySwapMappingStatus.REOPENED || next == PrimarySwapMappingStatus.ARCHIVED
            PrimarySwapMappingStatus.REOPENED -> return next == PrimarySwapMappingStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimarySwapMappingStatus) {
        if (!canMoveTo(next)) {
            error("primary swap mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round(maximum * margin * 100) / 100

    fun snapshot(): PrimarySwapMapping {
        return PrimarySwapMapping(
            code = code,
            title = title,
            carrier = carrier,
            score = score,
            externalRef = externalRef,
            phone = phone,
            active = active,
            maximum = maximum,
            margin = margin,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary swap mapping in status open. */
fun newPrimarySwapMapping(code: String, title: String): PrimarySwapMapping {
    return PrimarySwapMapping(
        code = code,
        title = title,
        carrier = "",
        score = 0.0,
        externalRef = "",
        phone = "",
        active = false,
        maximum = 0.0,
        margin = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = PrimarySwapMappingStatus.OPEN,
    )
}
