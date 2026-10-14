package com.acme.transport.linehaul.relay.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered relay record is in its life cycle. */
enum class TieredRelayRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered relay record as the relay team keeps it. */
data class TieredRelayRecord(
    var code: String,
    var name: String,
    var createdAt: Int,
    var revision: Int,
    var locale: String,
    var stopCount: Int,
    var sequence: Int,
    var status: TieredRelayRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRelayRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRelayRecordStatus): Boolean {
        when (status) {
            TieredRelayRecordStatus.OPEN -> {
                when (next) {
                    TieredRelayRecordStatus.RESOLVED, TieredRelayRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredRelayRecordStatus.IN_REVIEW -> {
                when (next) {
                    TieredRelayRecordStatus.REOPENED, TieredRelayRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredRelayRecordStatus.RESOLVED -> {
                when (next) {
                    TieredRelayRecordStatus.REOPENED, TieredRelayRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredRelayRecordStatus.REOPENED -> return next == TieredRelayRecordStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredRelayRecordStatus) {
        if (!canMoveTo(next)) {
            error("tiered relay record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round(revision.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): TieredRelayRecord {
        return TieredRelayRecord(
            code = code,
            name = name,
            createdAt = createdAt,
            revision = revision,
            locale = locale,
            stopCount = stopCount,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered relay record in status open. */
fun newTieredRelayRecord(code: String, name: String): TieredRelayRecord {
    return TieredRelayRecord(
        code = code,
        name = name,
        createdAt = 0,
        revision = 0,
        locale = "",
        stopCount = 0,
        sequence = 0,
        status = TieredRelayRecordStatus.OPEN,
    )
}
