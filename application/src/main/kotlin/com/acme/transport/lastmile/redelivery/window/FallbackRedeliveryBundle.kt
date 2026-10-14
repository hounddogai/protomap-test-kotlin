package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.text.normalize

/** Where a fallback redelivery bundle is in its life cycle. */
enum class FallbackRedeliveryBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback redelivery bundle as the redelivery team keeps it. */
data class FallbackRedeliveryBundle(
    var code: String,
    var label: String,
    var checksum: String,
    var trailerId: String,
    var ownerId: String,
    var rate: Double,
    var locale: String,
    var status: FallbackRedeliveryBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRedeliveryBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRedeliveryBundleStatus): Boolean {
        when (status) {
            FallbackRedeliveryBundleStatus.NEW -> return next == FallbackRedeliveryBundleStatus.QUEUED
            FallbackRedeliveryBundleStatus.QUEUED -> {
                when (next) {
                    FallbackRedeliveryBundleStatus.RUNNING -> return true
                }
                return false
            }
            FallbackRedeliveryBundleStatus.RUNNING -> return next == FallbackRedeliveryBundleStatus.DONE
            FallbackRedeliveryBundleStatus.DONE -> return next == FallbackRedeliveryBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackRedeliveryBundleStatus) {
        if (!canMoveTo(next)) {
            error("fallback redelivery bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = rate

    fun snapshot(): FallbackRedeliveryBundle {
        return FallbackRedeliveryBundle(
            code = code,
            label = label,
            checksum = checksum,
            trailerId = trailerId,
            ownerId = ownerId,
            rate = rate,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback redelivery bundle in status new. */
fun newFallbackRedeliveryBundle(code: String, label: String): FallbackRedeliveryBundle {
    return FallbackRedeliveryBundle(
        code = code,
        label = label,
        checksum = "",
        trailerId = "",
        ownerId = "",
        rate = 0.0,
        locale = "",
        status = FallbackRedeliveryBundleStatus.NEW,
    )
}
