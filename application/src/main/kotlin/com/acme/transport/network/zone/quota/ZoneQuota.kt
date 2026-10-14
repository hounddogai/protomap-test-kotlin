package com.acme.transport.network.zone.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone quota records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone quota is in its life cycle. */
enum class ZoneQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A zone quota as the zone team keeps it. */
data class ZoneQuota(
    var code: String,
    var label: String,
    var batchSize: Int,
    var phone: String,
    var dock: String,
    var lane: String,
    var timezone: String,
    var summary: String,
    var checksum: String,
    var sequence: Int,
    var status: ZoneQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneQuotaStatus): Boolean {
        when (status) {
            ZoneQuotaStatus.PLANNED -> return next == ZoneQuotaStatus.IN_PROGRESS || next == ZoneQuotaStatus.CANCELLED
            ZoneQuotaStatus.SCHEDULED -> {
                when (next) {
                    ZoneQuotaStatus.COMPLETED, ZoneQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            ZoneQuotaStatus.IN_PROGRESS -> {
                when (next) {
                    ZoneQuotaStatus.COMPLETED, ZoneQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            ZoneQuotaStatus.COMPLETED -> {
                when (next) {
                    ZoneQuotaStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneQuotaStatus) {
        if (!canMoveTo(next)) {
            error("zone quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveValue(): Double = round((batchSize.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): ZoneQuota {
        return ZoneQuota(
            code = code,
            label = label,
            batchSize = batchSize,
            phone = phone,
            dock = dock,
            lane = lane,
            timezone = timezone,
            summary = summary,
            checksum = checksum,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a zone quota in status planned. */
fun newZoneQuota(code: String, label: String): ZoneQuota {
    return ZoneQuota(
        code = code,
        label = label,
        batchSize = 0,
        phone = "",
        dock = "",
        lane = "",
        timezone = "",
        summary = "",
        checksum = "",
        sequence = 0,
        status = ZoneQuotaStatus.PLANNED,
    )
}
