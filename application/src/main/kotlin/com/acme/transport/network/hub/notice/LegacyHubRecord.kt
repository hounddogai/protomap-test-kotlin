package com.acme.transport.network.hub.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy hub record is in its life cycle. */
enum class LegacyHubRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy hub record and its current status. */
data class LegacyHubRecord(
    var code: String,
    var title: String,
    var currency: String,
    var ownerId: String,
    var checksum: String,
    var priority: Int,
    var capacity: Int,
    var weight: Double,
    var loadKg: Double,
    var status: LegacyHubRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyHubRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyHubRecordStatus): Boolean {
        when (status) {
            LegacyHubRecordStatus.NEW -> return next == LegacyHubRecordStatus.RUNNING
            LegacyHubRecordStatus.QUEUED -> return next == LegacyHubRecordStatus.RUNNING || next == LegacyHubRecordStatus.DONE
            LegacyHubRecordStatus.RUNNING -> {
                when (next) {
                    LegacyHubRecordStatus.DONE, LegacyHubRecordStatus.FAILED -> return true
                }
                return false
            }
            LegacyHubRecordStatus.DONE -> return next == LegacyHubRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyHubRecordStatus) {
        if (!canMoveTo(next)) {
            error("legacy hub record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy hub record"} $code"

    fun netValue(): Double = round(capacity.toDouble() * loadKg * 100) / 100

    fun snapshot(): LegacyHubRecord {
        return LegacyHubRecord(
            code = code,
            title = title,
            currency = currency,
            ownerId = ownerId,
            checksum = checksum,
            priority = priority,
            capacity = capacity,
            weight = weight,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy hub record in status new. */
fun newLegacyHubRecord(code: String, title: String): LegacyHubRecord {
    return LegacyHubRecord(
        code = code,
        title = title,
        currency = "",
        ownerId = "",
        checksum = "",
        priority = 0,
        capacity = 0,
        weight = 0.0,
        loadKg = 0.0,
        status = LegacyHubRecordStatus.NEW,
    )
}
