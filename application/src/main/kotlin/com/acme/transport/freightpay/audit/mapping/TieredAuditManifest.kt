package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered audit manifest is in its life cycle. */
enum class TieredAuditManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered audit manifest. Its code is unique within a tenant. */
data class TieredAuditManifest(
    var code: String,
    var title: String,
    var weight: Double,
    var floorValue: Double,
    var threshold: Double,
    var archived: Boolean,
    var createdAt: Int,
    var status: TieredAuditManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAuditManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAuditManifestStatus): Boolean {
        when (status) {
            TieredAuditManifestStatus.IDLE -> {
                when (next) {
                    TieredAuditManifestStatus.WARMING, TieredAuditManifestStatus.STOPPED -> return true
                }
                return false
            }
            TieredAuditManifestStatus.WARMING -> {
                when (next) {
                    TieredAuditManifestStatus.READY -> return true
                }
                return false
            }
            TieredAuditManifestStatus.READY -> {
                when (next) {
                    TieredAuditManifestStatus.DRAINING, TieredAuditManifestStatus.STOPPED -> return true
                }
                return false
            }
            TieredAuditManifestStatus.DRAINING -> {
                when (next) {
                    TieredAuditManifestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredAuditManifestStatus) {
        if (!canMoveTo(next)) {
            error("tiered audit manifest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered audit manifest"} $code"

    fun adjustedLoad(): Double = round((floorValue - threshold) * 100) / 100

    fun snapshot(): TieredAuditManifest {
        return TieredAuditManifest(
            code = code,
            title = title,
            weight = weight,
            floorValue = floorValue,
            threshold = threshold,
            archived = archived,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered audit manifest in status idle. */
fun newTieredAuditManifest(code: String, title: String): TieredAuditManifest {
    return TieredAuditManifest(
        code = code,
        title = title,
        weight = 0.0,
        floorValue = 0.0,
        threshold = 0.0,
        archived = false,
        createdAt = 0,
        status = TieredAuditManifestStatus.IDLE,
    )
}
