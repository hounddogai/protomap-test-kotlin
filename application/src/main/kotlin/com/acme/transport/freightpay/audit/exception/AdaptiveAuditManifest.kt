package com.acme.transport.freightpay.audit.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive audit manifest is in its life cycle. */
enum class AdaptiveAuditManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive audit manifest as the audit team keeps it. */
data class AdaptiveAuditManifest(
    var code: String,
    var name: String,
    var lane: String,
    var active: Boolean,
    var channel: String,
    var unit: String,
    var discount: Double,
    var pinned: Boolean,
    var ceiling: Double,
    var status: AdaptiveAuditManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAuditManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAuditManifestStatus): Boolean {
        when (status) {
            AdaptiveAuditManifestStatus.IDLE -> return next == AdaptiveAuditManifestStatus.READY
            AdaptiveAuditManifestStatus.WARMING -> {
                when (next) {
                    AdaptiveAuditManifestStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveAuditManifestStatus.READY -> return next == AdaptiveAuditManifestStatus.STOPPED
            AdaptiveAuditManifestStatus.DRAINING -> {
                when (next) {
                    AdaptiveAuditManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAuditManifestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive audit manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive audit manifest"} $code"

    fun effectiveTotal(): Double = round((discount + ceiling) * 100) / 100

    fun snapshot(): AdaptiveAuditManifest {
        return AdaptiveAuditManifest(
            code = code,
            name = name,
            lane = lane,
            active = active,
            channel = channel,
            unit = unit,
            discount = discount,
            pinned = pinned,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive audit manifest in status idle. */
fun newAdaptiveAuditManifest(code: String, name: String): AdaptiveAuditManifest {
    return AdaptiveAuditManifest(
        code = code,
        name = name,
        lane = "",
        active = false,
        channel = "",
        unit = "",
        discount = 0.0,
        pinned = false,
        ceiling = 0.0,
        status = AdaptiveAuditManifestStatus.IDLE,
    )
}
