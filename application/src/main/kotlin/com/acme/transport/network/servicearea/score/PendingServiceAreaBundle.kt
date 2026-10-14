package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending service area bundle is in its life cycle. */
enum class PendingServiceAreaBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending service area bundle. Its code is unique within a tenant. */
data class PendingServiceAreaBundle(
    var code: String,
    var name: String,
    var externalRef: String,
    var region: String,
    var margin: Double,
    var score: Double,
    var status: PendingServiceAreaBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingServiceAreaBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingServiceAreaBundleStatus): Boolean {
        when (status) {
            PendingServiceAreaBundleStatus.PROPOSED -> {
                when (next) {
                    PendingServiceAreaBundleStatus.APPROVED, PendingServiceAreaBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingServiceAreaBundleStatus.APPROVED -> return next == PendingServiceAreaBundleStatus.REJECTED || next == PendingServiceAreaBundleStatus.WITHDRAWN
            PendingServiceAreaBundleStatus.REJECTED -> return next == PendingServiceAreaBundleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingServiceAreaBundleStatus) {
        if (!canMoveTo(next)) {
            error("pending service area bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending service area bundle"} $code"

    fun weightedVolume(): Double = round((score - margin) * 100) / 100

    fun snapshot(): PendingServiceAreaBundle {
        return PendingServiceAreaBundle(
            code = code,
            name = name,
            externalRef = externalRef,
            region = region,
            margin = margin,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending service area bundle in status proposed. */
fun newPendingServiceAreaBundle(code: String, name: String): PendingServiceAreaBundle {
    return PendingServiceAreaBundle(
        code = code,
        name = name,
        externalRef = "",
        region = "",
        margin = 0.0,
        score = 0.0,
        status = PendingServiceAreaBundleStatus.PROPOSED,
    )
}
