package com.acme.transport.freightpay.audit.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional audit bundle is in its life cycle. */
enum class RegionalAuditBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One regional audit bundle, with what the planners need to act on it. */
data class RegionalAuditBundle(
    var code: String,
    var title: String,
    var checksum: String,
    var distanceKm: Double,
    var weight: Double,
    var quantity: Int,
    var approved: Boolean,
    var discount: Double,
    var driverId: String,
    var attributes: MutableMap<String, String>,
    var status: RegionalAuditBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAuditBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAuditBundleStatus): Boolean {
        when (status) {
            RegionalAuditBundleStatus.IDLE -> return next == RegionalAuditBundleStatus.READY || next == RegionalAuditBundleStatus.STOPPED
            RegionalAuditBundleStatus.WARMING -> return next == RegionalAuditBundleStatus.READY
            RegionalAuditBundleStatus.READY -> return next == RegionalAuditBundleStatus.DRAINING || next == RegionalAuditBundleStatus.STOPPED
            RegionalAuditBundleStatus.DRAINING -> return next == RegionalAuditBundleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RegionalAuditBundleStatus) {
        if (!canMoveTo(next)) {
            error("regional audit bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional audit bundle"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((distanceKm + discount) * 100) / 100

    fun snapshot(): RegionalAuditBundle {
        return RegionalAuditBundle(
            code = code,
            title = title,
            checksum = checksum,
            distanceKm = distanceKm,
            weight = weight,
            quantity = quantity,
            approved = approved,
            discount = discount,
            driverId = driverId,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional audit bundle in status idle. */
fun newRegionalAuditBundle(code: String, title: String): RegionalAuditBundle {
    return RegionalAuditBundle(
        code = code,
        title = title,
        checksum = "",
        distanceKm = 0.0,
        weight = 0.0,
        quantity = 0,
        approved = false,
        discount = 0.0,
        driverId = "",
        attributes = mutableMapOf<String, String>(),
        status = RegionalAuditBundleStatus.IDLE,
    )
}
