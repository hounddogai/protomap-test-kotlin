package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped service area variance is in its life cycle. */
enum class CappedServiceAreaVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One capped service area variance, with what the planners need to act on it. */
data class CappedServiceAreaVariance(
    var code: String,
    var label: String,
    var ownerId: String,
    var externalRef: String,
    var etaMinutes: Int,
    var approved: Boolean,
    var attributes: MutableMap<String, String>,
    var archived: Boolean,
    var margin: Double,
    var source: String,
    var status: CappedServiceAreaVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedServiceAreaVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedServiceAreaVarianceStatus): Boolean {
        when (status) {
            CappedServiceAreaVarianceStatus.IDLE -> {
                when (next) {
                    CappedServiceAreaVarianceStatus.DRAINING -> return true
                }
                return false
            }
            CappedServiceAreaVarianceStatus.WARMING -> return next == CappedServiceAreaVarianceStatus.DRAINING || next == CappedServiceAreaVarianceStatus.STOPPED
            CappedServiceAreaVarianceStatus.READY -> return next == CappedServiceAreaVarianceStatus.DRAINING || next == CappedServiceAreaVarianceStatus.STOPPED
            CappedServiceAreaVarianceStatus.DRAINING -> {
                when (next) {
                    CappedServiceAreaVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedServiceAreaVarianceStatus) {
        if (!canMoveTo(next)) {
            error("capped service area variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((margin - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedServiceAreaVariance {
        return CappedServiceAreaVariance(
            code = code,
            label = label,
            ownerId = ownerId,
            externalRef = externalRef,
            etaMinutes = etaMinutes,
            approved = approved,
            attributes = attributes,
            archived = archived,
            margin = margin,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped service area variance in status idle. */
fun newCappedServiceAreaVariance(code: String, label: String): CappedServiceAreaVariance {
    return CappedServiceAreaVariance(
        code = code,
        label = label,
        ownerId = "",
        externalRef = "",
        etaMinutes = 0,
        approved = false,
        attributes = mutableMapOf<String, String>(),
        archived = false,
        margin = 0.0,
        source = "",
        status = CappedServiceAreaVarianceStatus.IDLE,
    )
}
