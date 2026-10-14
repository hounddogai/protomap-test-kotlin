package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient lease return manifest is in its life cycle. */
enum class LenientLeaseReturnManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lenient lease return manifest, with what the planners need to act on it. */
data class LenientLeaseReturnManifest(
    var code: String,
    var title: String,
    var limit: Int,
    var dock: String,
    var label: String,
    var reviewer: String,
    var rate: Double,
    var externalRef: String,
    var status: LenientLeaseReturnManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientLeaseReturnManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientLeaseReturnManifestStatus): Boolean {
        when (status) {
            LenientLeaseReturnManifestStatus.PLANNED -> {
                when (next) {
                    LenientLeaseReturnManifestStatus.SCHEDULED, LenientLeaseReturnManifestStatus.COMPLETED -> return true
                }
                return false
            }
            LenientLeaseReturnManifestStatus.SCHEDULED -> return next == LenientLeaseReturnManifestStatus.IN_PROGRESS || next == LenientLeaseReturnManifestStatus.COMPLETED
            LenientLeaseReturnManifestStatus.IN_PROGRESS -> {
                when (next) {
                    LenientLeaseReturnManifestStatus.COMPLETED -> return true
                }
                return false
            }
            LenientLeaseReturnManifestStatus.COMPLETED -> {
                when (next) {
                    LenientLeaseReturnManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientLeaseReturnManifestStatus) {
        if (!canMoveTo(next)) {
            error("lenient lease return manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient lease return manifest"} $code"

    fun weightedTotal(): Double = round((rate + limit.toDouble()) * 100) / 100

    fun snapshot(): LenientLeaseReturnManifest {
        return LenientLeaseReturnManifest(
            code = code,
            title = title,
            limit = limit,
            dock = dock,
            label = label,
            reviewer = reviewer,
            rate = rate,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient lease return manifest in status planned. */
fun newLenientLeaseReturnManifest(code: String, title: String): LenientLeaseReturnManifest {
    return LenientLeaseReturnManifest(
        code = code,
        title = title,
        limit = 0,
        dock = "",
        label = "",
        reviewer = "",
        rate = 0.0,
        externalRef = "",
        status = LenientLeaseReturnManifestStatus.PLANNED,
    )
}
