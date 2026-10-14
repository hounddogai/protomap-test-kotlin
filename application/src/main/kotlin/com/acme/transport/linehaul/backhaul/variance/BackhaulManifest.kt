package com.acme.transport.linehaul.backhaul.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul manifest is in its life cycle. */
enum class BackhaulManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A backhaul manifest. Its code is unique within a tenant. */
data class BackhaulManifest(
    var code: String,
    var label: String,
    var checksum: String,
    var loadKg: Double,
    var pallets: Int,
    var cost: Double,
    var externalRef: String,
    var stopCount: Int,
    var status: BackhaulManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulManifestStatus): Boolean {
        when (status) {
            BackhaulManifestStatus.PROPOSED -> {
                when (next) {
                    BackhaulManifestStatus.REJECTED, BackhaulManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            BackhaulManifestStatus.APPROVED -> {
                when (next) {
                    BackhaulManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            BackhaulManifestStatus.REJECTED -> return next == BackhaulManifestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BackhaulManifestStatus) {
        if (!canMoveTo(next)) {
            error("backhaul manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round(loadKg * stopCount.toDouble() * 100) / 100

    fun snapshot(): BackhaulManifest {
        return BackhaulManifest(
            code = code,
            label = label,
            checksum = checksum,
            loadKg = loadKg,
            pallets = pallets,
            cost = cost,
            externalRef = externalRef,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a backhaul manifest in status proposed. */
fun newBackhaulManifest(code: String, label: String): BackhaulManifest {
    return BackhaulManifest(
        code = code,
        label = label,
        checksum = "",
        loadKg = 0.0,
        pallets = 0,
        cost = 0.0,
        externalRef = "",
        stopCount = 0,
        status = BackhaulManifestStatus.PROPOSED,
    )
}
