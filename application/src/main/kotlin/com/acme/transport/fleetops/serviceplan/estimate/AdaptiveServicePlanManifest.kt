package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive service plan manifest is in its life cycle. */
enum class AdaptiveServicePlanManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive service plan manifest as the service plan team keeps it. */
data class AdaptiveServicePlanManifest(
    var code: String,
    var title: String,
    var attempts: Int,
    var category: String,
    var margin: Double,
    var externalRef: String,
    var segment: String,
    var ownerId: String,
    var pallets: Int,
    var quantity: Int,
    var status: AdaptiveServicePlanManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveServicePlanManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveServicePlanManifestStatus): Boolean {
        when (status) {
            AdaptiveServicePlanManifestStatus.IDLE -> {
                when (next) {
                    AdaptiveServicePlanManifestStatus.WARMING -> return true
                }
                return false
            }
            AdaptiveServicePlanManifestStatus.WARMING -> return next == AdaptiveServicePlanManifestStatus.DRAINING
            AdaptiveServicePlanManifestStatus.READY -> return next == AdaptiveServicePlanManifestStatus.DRAINING || next == AdaptiveServicePlanManifestStatus.STOPPED
            AdaptiveServicePlanManifestStatus.DRAINING -> {
                when (next) {
                    AdaptiveServicePlanManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveServicePlanManifestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive service plan manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive service plan manifest"} $code"

    fun effectiveVolume(): Double = round((quantity.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveServicePlanManifest {
        return AdaptiveServicePlanManifest(
            code = code,
            title = title,
            attempts = attempts,
            category = category,
            margin = margin,
            externalRef = externalRef,
            segment = segment,
            ownerId = ownerId,
            pallets = pallets,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive service plan manifest in status idle. */
fun newAdaptiveServicePlanManifest(code: String, title: String): AdaptiveServicePlanManifest {
    return AdaptiveServicePlanManifest(
        code = code,
        title = title,
        attempts = 0,
        category = "",
        margin = 0.0,
        externalRef = "",
        segment = "",
        ownerId = "",
        pallets = 0,
        quantity = 0,
        status = AdaptiveServicePlanManifestStatus.IDLE,
    )
}
