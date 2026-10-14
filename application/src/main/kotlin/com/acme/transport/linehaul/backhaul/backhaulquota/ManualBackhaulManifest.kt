package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual backhaul manifest is in its life cycle. */
enum class ManualBackhaulManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a manual backhaul manifest and its current status. */
data class ManualBackhaulManifest(
    var code: String,
    var name: String,
    var lane: String,
    var ownerId: String,
    var stopCount: Int,
    var limit: Int,
    var status: ManualBackhaulManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualBackhaulManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualBackhaulManifestStatus): Boolean {
        when (status) {
            ManualBackhaulManifestStatus.OPEN -> return next == ManualBackhaulManifestStatus.IN_REVIEW
            ManualBackhaulManifestStatus.IN_REVIEW -> {
                when (next) {
                    ManualBackhaulManifestStatus.RESOLVED -> return true
                }
                return false
            }
            ManualBackhaulManifestStatus.RESOLVED -> {
                when (next) {
                    ManualBackhaulManifestStatus.REOPENED, ManualBackhaulManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualBackhaulManifestStatus.REOPENED -> {
                when (next) {
                    ManualBackhaulManifestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualBackhaulManifestStatus) {
        if (!canMoveTo(next)) {
            error("manual backhaul manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round((stopCount.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): ManualBackhaulManifest {
        return ManualBackhaulManifest(
            code = code,
            name = name,
            lane = lane,
            ownerId = ownerId,
            stopCount = stopCount,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual backhaul manifest in status open. */
fun newManualBackhaulManifest(code: String, name: String): ManualBackhaulManifest {
    return ManualBackhaulManifest(
        code = code,
        name = name,
        lane = "",
        ownerId = "",
        stopCount = 0,
        limit = 0,
        status = ManualBackhaulManifestStatus.OPEN,
    )
}
