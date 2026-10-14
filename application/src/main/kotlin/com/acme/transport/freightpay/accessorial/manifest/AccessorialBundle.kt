package com.acme.transport.freightpay.accessorial.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial bundle is in its life cycle. */
enum class AccessorialBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A accessorial bundle. Its code is unique within a tenant. */
data class AccessorialBundle(
    var code: String,
    var label: String,
    var margin: Double,
    var discount: Double,
    var loadKg: Double,
    var status: AccessorialBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialBundleStatus): Boolean {
        when (status) {
            AccessorialBundleStatus.IDLE -> return next == AccessorialBundleStatus.WARMING || next == AccessorialBundleStatus.READY
            AccessorialBundleStatus.WARMING -> {
                when (next) {
                    AccessorialBundleStatus.READY, AccessorialBundleStatus.DRAINING -> return true
                }
                return false
            }
            AccessorialBundleStatus.READY -> {
                when (next) {
                    AccessorialBundleStatus.DRAINING, AccessorialBundleStatus.STOPPED -> return true
                }
                return false
            }
            AccessorialBundleStatus.DRAINING -> {
                when (next) {
                    AccessorialBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialBundleStatus) {
        if (!canMoveTo(next)) {
            error("accessorial bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"accessorial bundle"} $code"

    fun weightedTotal(): Double = round((discount + loadKg) * 100) / 100

    fun snapshot(): AccessorialBundle {
        return AccessorialBundle(
            code = code,
            label = label,
            margin = margin,
            discount = discount,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a accessorial bundle in status idle. */
fun newAccessorialBundle(code: String, label: String): AccessorialBundle {
    return AccessorialBundle(
        code = code,
        label = label,
        margin = 0.0,
        discount = 0.0,
        loadKg = 0.0,
        status = AccessorialBundleStatus.IDLE,
    )
}
