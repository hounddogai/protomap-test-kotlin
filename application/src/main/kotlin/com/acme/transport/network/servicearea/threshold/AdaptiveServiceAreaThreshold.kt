package com.acme.transport.network.servicearea.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive service area threshold records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a adaptive service area threshold is in its life cycle. */
enum class AdaptiveServiceAreaThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive service area threshold. Its code is unique within a tenant. */
data class AdaptiveServiceAreaThreshold(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var revision: Int,
    var carrier: String,
    var ratio: Double,
    var archived: Boolean,
    var externalRef: String,
    var visible: Boolean,
    var limit: Int,
    var status: AdaptiveServiceAreaThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveServiceAreaThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveServiceAreaThresholdStatus): Boolean {
        when (status) {
            AdaptiveServiceAreaThresholdStatus.REQUESTED -> return next == AdaptiveServiceAreaThresholdStatus.EXPIRED
            AdaptiveServiceAreaThresholdStatus.GRANTED -> {
                when (next) {
                    AdaptiveServiceAreaThresholdStatus.REVOKED, AdaptiveServiceAreaThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveServiceAreaThresholdStatus.REVOKED -> {
                when (next) {
                    AdaptiveServiceAreaThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveServiceAreaThresholdStatus) {
        if (!canMoveTo(next)) {
            error("adaptive service area threshold $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((etaMinutes.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveServiceAreaThreshold {
        return AdaptiveServiceAreaThreshold(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            revision = revision,
            carrier = carrier,
            ratio = ratio,
            archived = archived,
            externalRef = externalRef,
            visible = visible,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive service area threshold in status requested. */
fun newAdaptiveServiceAreaThreshold(code: String, name: String): AdaptiveServiceAreaThreshold {
    return AdaptiveServiceAreaThreshold(
        code = code,
        name = name,
        etaMinutes = 0,
        revision = 0,
        carrier = "",
        ratio = 0.0,
        archived = false,
        externalRef = "",
        visible = false,
        limit = 0,
        status = AdaptiveServiceAreaThresholdStatus.REQUESTED,
    )
}
